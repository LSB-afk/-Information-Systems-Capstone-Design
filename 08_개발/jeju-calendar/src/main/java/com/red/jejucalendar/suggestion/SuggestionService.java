package com.red.jejucalendar.suggestion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.analysis.AnalysisDtos.MonthValue;
import com.red.jejucalendar.analysis.AnalysisDtos.SourceInfo;
import com.red.jejucalendar.analysis.RegionSelector;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.PromoPlanDao;
import com.red.jejucalendar.dao.PromoSuggestionDao;
import com.red.jejucalendar.dao.PromoSuggestionItemDao;
import com.red.jejucalendar.dao.RecommendationCriteriaDao;
import com.red.jejucalendar.dao.RegionConsumptionStatDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.PromoPlan;
import com.red.jejucalendar.domain.PromoSuggestion;
import com.red.jejucalendar.domain.PromoSuggestionItem;
import com.red.jejucalendar.domain.RecommendationCriteria;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionConsumptionStat;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.plan.CalendarPeriod;
import com.red.jejucalendar.suggestion.SuggestionDtos.Card;
import com.red.jejucalendar.suggestion.SuggestionDtos.CriteriaSummary;
import com.red.jejucalendar.suggestion.SuggestionDtos.Detail;
import com.red.jejucalendar.suggestion.SuggestionDtos.Item;
import com.red.jejucalendar.suggestion.SuggestionDtos.ListResponse;
import com.red.jejucalendar.suggestion.SuggestionDtos.MonthSuggestions;
import com.red.jejucalendar.suggestion.SuggestionDtos.RecalcResult;

/**
 * UC02 월별 홍보 제안 조회, UC10 자료 출처·기준 표시 (SCR-C-004, SCR-C-004-P01)
 * 제안은 추천 기준 저장·방문 자료 등록 때 다시 계산해 PROMO_SUGGESTION에 저장하고, 조회는 저장된 제안만 읽음
 * 지역·대상 월마다 제안은 하나이며, 다시 계산해도 남는 제안은 같은 번호를 유지해 계획과의 연결이 끊기지 않음
 */
@Service
public class SuggestionService {

    static final String VISIT = "VISIT";
    static final String NOTICE = "추천은 선택할 수 있는 아이디어입니다. 매출이나 방문 증가가 확인된 결과는 아닙니다.";

    private final RecommendationCriteriaDao criteriaDao;
    private final RegionMonthlyStatDao monthlyDao;
    private final RegionConsumptionStatDao consumptionDao;
    private final PromoSuggestionDao suggestionDao;
    private final PromoSuggestionItemDao itemDao;
    private final PromoPlanDao planDao;
    private final DataSourceFileDao sourceDao;
    private final BusinessAccessService accessService;
    private final RegionSelector regionSelector;
    private final CalendarPeriod period;

    public SuggestionService(RecommendationCriteriaDao criteriaDao, RegionMonthlyStatDao monthlyDao,
            RegionConsumptionStatDao consumptionDao, PromoSuggestionDao suggestionDao, PromoSuggestionItemDao itemDao,
            PromoPlanDao planDao, DataSourceFileDao sourceDao, BusinessAccessService accessService,
            RegionSelector regionSelector, CalendarPeriod period) {
        this.criteriaDao = criteriaDao;
        this.monthlyDao = monthlyDao;
        this.consumptionDao = consumptionDao;
        this.suggestionDao = suggestionDao;
        this.itemDao = itemDao;
        this.planDao = planDao;
        this.sourceDao = sourceDao;
        this.accessService = accessService;
        this.regionSelector = regionSelector;
        this.period = period;
    }

    // ---------- 다시 계산 ----------

    /**
     * 현재 추천 기준과 가장 최근 연도의 방문 자료로 일정표 기간의 제안을 다시 만듦
     * 기준을 넘는 지역·월: 새로 만들거나 같은 번호로 고침 / 넘지 않게 된 제안: 항목과 함께 지움
     * 지운 제안으로 만든 계획은 남고 제안 연결만 비워짐 (FK ON DELETE SET NULL)
     */
    @Transactional
    public RecalcResult recalculate() {
        Optional<RecommendationCriteria> criteria = criteriaDao.findCurrent();
        Map<String, PromoSuggestion> existing = new HashMap<>();
        suggestionDao.findByTargetMonthBetween(period.start(), lastTargetMonth())
                .forEach(s -> existing.put(key(s.regionCode(), YearMonth.from(s.targetMonth())), s));

        int created = 0;
        int updated = 0;
        List<Integer> years = monthlyDao.findYears(VISIT);
        if (criteria.isPresent() && !years.isEmpty()) {
            RecommendationCriteria c = criteria.get();
            int baseYear = years.get(0);
            long sourceId = monthlyDao.findLatestSourceId(VISIT, baseYear).orElseThrow();
            for (Map.Entry<String, Map<Integer, BigDecimal>> region : visitsByRegion(sourceId, baseYear).entrySet()) {
                for (SuggestionRules.Decision d : SuggestionRules.decide(c.declineRateThreshold(), baseYear,
                        region.getValue(), period.months())) {
                    PromoSuggestion old = existing.remove(key(region.getKey(), d.target()));
                    PromoSuggestion next = build(old == null ? null : old.suggestionId(), c, sourceId, region.getKey(), d);
                    long id;
                    if (old == null) {
                        id = suggestionDao.insert(next);
                        created++;
                    } else {
                        suggestionDao.update(next);
                        itemDao.deleteBySuggestionId(old.suggestionId());
                        id = old.suggestionId();
                        updated++;
                    }
                    for (Item item : items(region.getKey(), d)) {
                        itemDao.insert(new PromoSuggestionItem(id, item.itemType(), item.content(), item.reason(),
                                item.checkNote()));
                    }
                }
            }
        }
        for (PromoSuggestion stale : existing.values()) {
            itemDao.deleteBySuggestionId(stale.suggestionId());
            suggestionDao.deleteById(stale.suggestionId());
        }
        String message = criteria.isEmpty() ? "추천 기준이 없어 제안을 만들지 않았습니다."
                : years.isEmpty() ? "검증을 통과한 방문 자료가 없어 제안을 만들지 않았습니다."
                : "제안을 다시 계산했습니다. (새 제안 " + created + "개, 수정 " + updated + "개, 삭제 " + existing.size() + "개)";
        return new RecalcResult(created, updated, existing.size(), message);
    }

    /** 서비스 운영자가 직접 다시 계산할 때 */
    public RecalcResult recalculate(LoginUser user) {
        user.requireServiceOperator();
        return recalculate();
    }

    private PromoSuggestion build(Long id, RecommendationCriteria c, long sourceId, String regionCode,
            SuggestionRules.Decision d) {
        SuggestionRules.Template t = SuggestionRules.template(d.target().getMonthValue());
        int day = Math.min(t.day(), d.target().lengthOfMonth());
        return new PromoSuggestion(id, c.criteriaId(), d.target().atDay(1), t.title(),
                SuggestionRules.reason(d, c.declineRateThreshold()), regionCode, t.description(), d.target().atDay(day),
                t.channel(), t.task(), t.tag(), sourceId);
    }

    /** 제안 카드의 네 항목 (자료가 없는 항목은 미확보로 적고 운영자 확인을 안내함) */
    private List<Item> items(String regionCode, SuggestionRules.Decision d) {
        SuggestionRules.Template t = SuggestionRules.template(d.target().getMonthValue());
        String unit = "만 명";
        List<Item> items = new ArrayList<>();
        items.add(new Item("TIMING", d.target().getYear() + "년 " + d.target().getMonthValue() + "월 · 과거 계절 흐름 참고",
                d.baseYear() + "년 " + d.previousMonth() + "월 " + plain(d.previousValue()) + unit + " → "
                        + d.target().getMonthValue() + "월 " + plain(d.currentValue()) + unit,
                "날짜·휴무일 확인 필요"));
        items.add(audience(regionCode, YearMonth.of(d.baseYear(), d.target().getMonthValue())));
        items.add(new Item("PRODUCT", t.product(), t.productReason(), "판매 가능 여부·원가 확인 필요"));
        items.add(new Item("ATTRACTION", "주변 관광지 자료 미확보", "인기 장소 자료를 아직 등록하지 않음",
                "거리·운영시간은 운영자가 확인"));
        return items;
    }

    /** 홍보 대상: 같은 달 카페 업종의 내·외국인 소비 비중이 있으면 그 값을 근거로 씀 */
    private Item audience(String regionCode, YearMonth baseMonth) {
        Optional<Long> source = consumptionDao.findLatestSourceId(regionCode, baseMonth.atDay(1));
        if (source.isPresent()) {
            List<RegionConsumptionStat> stats = consumptionDao.findByRegionAndMonth(source.get(), regionCode,
                    baseMonth.atDay(1));
            BigDecimal domestic = share(stats, "DOMESTIC");
            BigDecimal foreign = share(stats, "FOREIGN");
            if (domestic != null && foreign != null) {
                String content = domestic.compareTo(foreign) >= 0 ? "내국인 중심 안내 후보" : "외국인 대상 위치 안내 후보";
                return new Item("AUDIENCE", content, baseMonth.getYear() + "년 " + baseMonth.getMonthValue()
                        + "월 카페 카드 소비 내국인 " + plain(domestic) + "% · 외국인 " + plain(foreign) + "%",
                        "실제 고객 구성 확인 필요");
            }
        }
        return new Item("AUDIENCE", "홍보 대상 자료 미확보", "업종별 내·외국인 소비 비중 자료가 없음",
                "소비 비중 자료 등록 후 다시 계산");
    }

    // ---------- 조회 ----------

    /** SCR-C-004 ①②④⑥: 지역의 3개월 제안을 월별로 묶음 */
    public ListResponse list(LoginUser user, String regionCode, Long businessId) {
        Region region = regionSelector.select(user, regionCode);
        Optional<Business> business = business(user, businessId);
        Map<Long, Long> added = addedPlans(business);
        Optional<RecommendationCriteria> criteria = criteriaDao.findCurrent();

        Map<String, List<Card>> byMonth = new LinkedHashMap<>();
        period.months().forEach(m -> byMonth.put(m.toString(), new ArrayList<>()));
        int total = 0;
        for (PromoSuggestion s : suggestionDao.findByTargetMonthBetween(period.start(), lastTargetMonth())) {
            if (!s.regionCode().equals(region.regionCode())) {
                continue;
            }
            byMonth.get(YearMonth.from(s.targetMonth()).toString()).add(card(s, added));
            total++;
        }
        List<MonthSuggestions> months = byMonth.entrySet().stream()
                .map(e -> new MonthSuggestions(e.getKey(), e.getValue())).toList();
        String message = criteria.isEmpty() ? "추천 기준이 아직 없어 제안을 만들지 않았습니다."
                : total == 0 ? "조건에 맞는 제안이 없습니다. 선택 지역의 자료와 적용된 추천 기준을 확인해 주세요." : null;
        boolean canAdd = business.isPresent() && business.get().regionCode().equals(region.regionCode());
        return new ListResponse(region.regionCode(), region.regionName(), period.label(),
                criteria.map(SuggestionService::summary).orElse(null), business.map(Business::businessId).orElse(null),
                canAdd, total, message, months);
    }

    /** SCR-C-004-P01: 선택 제안의 이유·관측값·출처·조건·계획 행동 */
    public Detail detail(LoginUser user, long suggestionId, Long businessId) {
        PromoSuggestion s = suggestionDao.findById(suggestionId)
                .orElseThrow(() -> ApiException.notFound("현재 추천 조건에 맞지 않는 제안입니다. 다시 조회해 주세요."));
        Optional<Business> business = business(user, businessId);
        RecommendationCriteria c = criteriaDao.findById(s.criteriaId()).orElseThrow();
        // 제안을 계산할 때 쓴 자료의 가장 최근 연도 (계산은 그 연도의 값으로 함)
        int baseYear = monthlyDao.findBySourceId(s.sourceId()).stream()
                .map(r -> r.statMonth().getYear()).max(Integer::compare).orElse(s.targetMonth().getYear() - 1);
        Map<Integer, BigDecimal> values = new HashMap<>();
        for (RegionMonthlyStat stat : monthlyDao.findByRegionAndPeriod(s.sourceId(), s.regionCode(), VISIT,
                LocalDate.of(baseYear, 1, 1), LocalDate.of(baseYear, 12, 1))) {
            values.put(stat.statMonth().getMonthValue(), stat.statValue());
        }
        List<MonthValue> observed = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            observed.add(new MonthValue(m, values.get(m)));
        }
        boolean canAdd = business.isPresent() && business.get().regionCode().equals(s.regionCode());
        return new Detail(card(s, addedPlans(business)), summary(c), baseYear, observed,
                List.of("검증을 통과한 자료 사용 (기간·집계·지역 코드·누락값·제공 기준)"),
                List.of(c.requiredCondition(), "판매 가능 상품·비용·휴무일·접근 경로는 사업체가 확인"),
                canAdd, NOTICE);
    }

    private Card card(PromoSuggestion s, Map<Long, Long> added) {
        List<Item> items = itemDao.findBySuggestionId(s.suggestionId()).stream()
                .sorted((a, b) -> Integer.compare(order(a.itemType()), order(b.itemType())))
                .map(i -> new Item(i.itemType(), i.itemContent(), i.itemReason(), i.checkNote()))
                .toList();
        SourceInfo source = sourceDao.findById(s.sourceId()).map(SourceInfo::of).orElse(null);
        return new Card(s.suggestionId(), s.regionCode(), s.targetMonth(), s.title(), s.description(), s.tag(),
                s.reason(), s.channel(), s.recommendedDate(), s.preparationTask(), items, source,
                added.get(s.suggestionId()));
    }

    /** 일정에 담을 사업체: 지정한 사업체(권한 확인) 또는 처리할 수 있는 첫 사업체 (없으면 담기 기능 없음) */
    private Optional<Business> business(LoginUser user, Long businessId) {
        if (businessId != null) {
            return Optional.of(accessService.requireAccess(user, businessId));
        }
        return accessService.accessibleBusinesses(user).stream().findFirst();
    }

    private Map<Long, Long> addedPlans(Optional<Business> business) {
        if (business.isEmpty()) {
            return Map.of();
        }
        return planDao.findByBusinessId(business.get().businessId()).stream()
                .filter(p -> p.suggestionId() != null)
                .collect(Collectors.toMap(PromoPlan::suggestionId, PromoPlan::planId, (a, b) -> a));
    }

    private Map<String, Map<Integer, BigDecimal>> visitsByRegion(long sourceId, int year) {
        Map<String, Map<Integer, BigDecimal>> result = new LinkedHashMap<>();
        for (RegionMonthlyStat s : monthlyDao.findBySourceAndPeriod(sourceId, VISIT, LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 1))) {
            if (s.statValue() != null) {
                result.computeIfAbsent(s.regionCode(), k -> new HashMap<>()).put(s.statMonth().getMonthValue(),
                        s.statValue());
            }
        }
        return result;
    }

    private LocalDate lastTargetMonth() {
        List<YearMonth> months = period.months();
        return months.get(months.size() - 1).atDay(1);
    }

    private static CriteriaSummary summary(RecommendationCriteria c) {
        return new CriteriaSummary(c.criteriaId(), c.declineRateThreshold(), c.sourceInfo(), c.basePeriod(),
                c.reasonTemplate(), c.requiredCondition(), c.updatedAt());
    }

    private static String key(String regionCode, YearMonth month) {
        return regionCode + "|" + month;
    }

    private static int order(String itemType) {
        return switch (itemType) {
            case "TIMING" -> 0;
            case "AUDIENCE" -> 1;
            case "PRODUCT" -> 2;
            default -> 3;
        };
    }

    private static BigDecimal share(List<RegionConsumptionStat> stats, String segment) {
        return stats.stream()
                .filter(s -> "CAFE".equals(s.category()) && segment.equals(s.segment()) && "ALL".equals(s.timeSlot()))
                .map(RegionConsumptionStat::shareRate).filter(Objects::nonNull).findFirst().orElse(null);
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
