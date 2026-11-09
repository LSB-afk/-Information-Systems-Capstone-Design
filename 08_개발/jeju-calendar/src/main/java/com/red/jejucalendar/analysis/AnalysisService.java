package com.red.jejucalendar.analysis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.red.jejucalendar.analysis.AnalysisDtos.ConsumptionResponse;
import com.red.jejucalendar.analysis.AnalysisDtos.MonthValue;
import com.red.jejucalendar.analysis.AnalysisDtos.RegionValue;
import com.red.jejucalendar.analysis.AnalysisDtos.SeasonRow;
import com.red.jejucalendar.analysis.AnalysisDtos.SegmentRow;
import com.red.jejucalendar.analysis.AnalysisDtos.ShareSection;
import com.red.jejucalendar.analysis.AnalysisDtos.SourceInfo;
import com.red.jejucalendar.analysis.AnalysisDtos.TimeSlotRow;
import com.red.jejucalendar.analysis.AnalysisDtos.TrendResponse;
import com.red.jejucalendar.analysis.AnalysisDtos.VisitSpendRow;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.RegionConsumptionStatDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.datasource.VisitCsvValidator;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionConsumptionStat;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.plan.CalendarPeriod;

/**
 * UC01 지역별 방문·소비 분석 조회, UC10 자료 출처·기준 표시 (SCR-C-003)
 * 지표·연도마다 검증을 통과한 자료 중 가장 최근에 등록한 자료 하나를 골라 모든 지역에 같은 기준으로 씀
 * 지역 방문 집계를 개별 가게 손님 수로 보지 않고, 방문·소비로 전환율·1인당 소비액을 계산하지 않음
 */
@Service
public class AnalysisService {

    public static final String VISIT = "VISIT";
    public static final String SPEND = "SPEND";
    static final String VISIT_NOTICE = "지역 방문 집계는 개별 가게의 실제 손님 수가 아닙니다.";
    static final String CONSUMPTION_NOTICE =
            "방문(만 명)과 카드 소비(억 원)는 모집단이 달라 각 지표의 변화만 비교합니다. 전환율·1인당 소비액은 계산하지 않습니다.";
    private static final Map<String, String> TIME_SLOT_LABELS = new LinkedHashMap<>();

    static {
        TIME_SLOT_LABELS.put("T0611", "06시부터 11시");
        TIME_SLOT_LABELS.put("T1114", "11시부터 14시");
        TIME_SLOT_LABELS.put("T1418", "14시부터 18시");
        TIME_SLOT_LABELS.put("T1806", "18시부터 다음 날 06시");
    }

    private final RegionDao regionDao;
    private final RegionMonthlyStatDao monthlyDao;
    private final RegionConsumptionStatDao consumptionDao;
    private final DataSourceFileDao sourceDao;
    private final BusinessAccessService accessService;
    private final CalendarPeriod period;

    public AnalysisService(RegionDao regionDao, RegionMonthlyStatDao monthlyDao, RegionConsumptionStatDao consumptionDao,
            DataSourceFileDao sourceDao, BusinessAccessService accessService, CalendarPeriod period) {
        this.regionDao = regionDao;
        this.monthlyDao = monthlyDao;
        this.consumptionDao = consumptionDao;
        this.sourceDao = sourceDao;
        this.accessService = accessService;
        this.period = period;
    }

    /** SCR-C-003 방문·계절 비교 ①②④⑤⑥⑦⑧⑨ */
    public TrendResponse trend(LoginUser user, String regionCode, Integer year, String metric) {
        String metricType = metric == null || metric.isBlank() ? VISIT : metric;
        if (!VISIT.equals(metricType) && !SPEND.equals(metricType)) {
            throw ApiException.badRequest("metric", "지표는 방문(VISIT) 또는 소비(SPEND)만 고를 수 있어요.");
        }
        Region region = region(user, regionCode);
        List<Integer> years = monthlyDao.findYears(metricType);
        int targetYear = year != null ? year : years.isEmpty() ? period.start().getYear() - 1 : years.get(0);
        String unitLabel = VISIT.equals(metricType) ? "만 명" : "억 원";
        int startMonth = period.start().getMonthValue();
        String changeLabel = startMonth + "월 → " + (startMonth % 12 + 1) + "월";

        Optional<Long> sourceId = monthlyDao.findLatestSourceId(metricType, targetYear);
        if (sourceId.isEmpty()) {
            return new TrendResponse(region.regionCode(), region.regionName(), targetYear, metricType, unitLabel, years,
                    false, noDataMessage(targetYear, years), null, List.of(), null, null, List.of(), changeLabel,
                    List.of(), VISIT_NOTICE);
        }
        Map<String, Map<Integer, Integer>> byRegion = valuesByRegion(sourceId.get(), metricType, targetYear);
        List<MonthValue> months = twelve(byRegion.get(region.regionCode()));
        boolean hasRegionData = months.stream().anyMatch(m -> m.value() != null);

        List<RegionValue> regions = new ArrayList<>();
        List<SeasonRow> season = new ArrayList<>();
        for (Region r : regionDao.findAll()) {
            Map<Integer, Integer> values = byRegion.get(r.regionCode());
            if (values == null) {
                continue;
            }
            List<MonthValue> m = twelve(values);
            regions.add(new RegionValue(r.regionCode(), r.regionName(), values.get(startMonth)));
            season.add(new SeasonRow(r.regionCode(), r.regionName(), peak(m), low(m),
                    changeRate(values.get(startMonth), values.get(startMonth % 12 + 1))));
        }
        String message = hasRegionData ? null
                : region.regionName() + "의 " + targetYear + "년 자료가 없습니다. 다른 지역을 선택해 주세요.";
        return new TrendResponse(region.regionCode(), region.regionName(), targetYear, metricType, unitLabel, years,
                hasRegionData, message, sourceInfo(sourceId.get()), hasRegionData ? months : List.of(),
                hasRegionData ? peak(months) : null, hasRegionData ? low(months) : null, regions, changeLabel, season,
                VISIT_NOTICE);
    }

    /**
     * SCR-C-003 소비 비교 ①②③④⑤⑥
     * 내·외국인·시간대 비중은 계획 시작 월(기본 10월)과 같은 달의 자료를 씀
     */
    public ConsumptionResponse consumption(LoginUser user, String regionCode, Integer year, String category) {
        Region region = region(user, regionCode);
        TreeSet<Integer> yearSet = new TreeSet<>(Comparator.reverseOrder());
        yearSet.addAll(monthlyDao.findYears(VISIT));
        yearSet.addAll(monthlyDao.findYears(SPEND));
        List<Integer> years = List.copyOf(yearSet);
        int targetYear = year != null ? year : years.isEmpty() ? period.start().getYear() - 1 : years.get(0);
        int shareMonth = period.start().getMonthValue();

        Optional<Long> visitSource = monthlyDao.findLatestSourceId(VISIT, targetYear);
        Optional<Long> spendSource = monthlyDao.findLatestSourceId(SPEND, targetYear);
        Map<Integer, Integer> visits = visitSource
                .map(id -> valuesByRegion(id, VISIT, targetYear).get(region.regionCode())).orElse(null);
        Map<Integer, Integer> spends = spendSource
                .map(id -> valuesByRegion(id, SPEND, targetYear).get(region.regionCode())).orElse(null);
        List<VisitSpendRow> rows = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Integer v = visits == null ? null : visits.get(m);
            Integer s = spends == null ? null : spends.get(m);
            String note = v != null && s != null ? "각 지표의 변화만 비교"
                    : v == null && s == null ? "방문·소비 모두 미확보"
                    : v == null ? "방문 비교 불가 · 0으로 대체하지 않음" : "소비 비교 불가 · 0으로 대체하지 않음";
            rows.add(new VisitSpendRow(m, v, s, note));
        }

        String timeSlotCategory = category == null || category.isBlank() ? "CAFE" : category;
        ShareSection shares = shares(region, YearMonth.of(targetYear, shareMonth), timeSlotCategory);
        return new ConsumptionResponse(region.regionCode(), region.regionName(), targetYear, shareMonth, years,
                visitSource.map(this::sourceInfo).orElse(null), spendSource.map(this::sourceInfo).orElse(null),
                rows, shares, CONSUMPTION_NOTICE);
    }

    private ShareSection shares(Region region, YearMonth month, String timeSlotCategory) {
        Optional<Long> sourceId = consumptionDao.findLatestSourceId(region.regionCode(), month.atDay(1));
        if (sourceId.isEmpty()) {
            return new ShareSection(false, month.getYear() + "년 " + month.getMonthValue()
                    + "월 업종·내외국인·시간대별 소비 비중 자료가 없습니다.", null, List.of(), timeSlotCategory, List.of());
        }
        List<RegionConsumptionStat> stats = consumptionDao.findByRegionAndMonth(sourceId.get(), region.regionCode(),
                month.atDay(1));
        Map<String, List<RegionConsumptionStat>> byCategory = stats.stream()
                .filter(s -> "ALL".equals(s.timeSlot()))
                .collect(Collectors.groupingBy(RegionConsumptionStat::category, LinkedHashMap::new, Collectors.toList()));
        List<SegmentRow> segments = new ArrayList<>();
        byCategory.forEach((category, list) -> {
            BigDecimal domestic = share(list, "DOMESTIC");
            BigDecimal foreign = share(list, "FOREIGN");
            boolean comparable = domestic != null && foreign != null;
            segments.add(new SegmentRow(category, domestic, foreign, comparable,
                    comparable ? null : "내국인·외국인 중 한쪽 값이 없어 비교를 제한합니다."));
        });
        List<TimeSlotRow> timeSlots = new ArrayList<>();
        TIME_SLOT_LABELS.forEach((slot, label) -> stats.stream()
                .filter(s -> timeSlotCategory.equals(s.category()) && "ALL".equals(s.segment()) && slot.equals(s.timeSlot()))
                .findFirst()
                .ifPresent(s -> timeSlots.add(new TimeSlotRow(slot, label, s.shareRate()))));
        return new ShareSection(true, null, sourceInfo(sourceId.get()), segments, timeSlotCategory, timeSlots);
    }

    /** 지역을 고르지 않았으면 사용자의 첫 사업체 지역, 없으면 첫 번째 지역 */
    private Region region(LoginUser user, String regionCode) {
        if (regionCode != null && !regionCode.isBlank()) {
            return regionDao.findByCode(regionCode)
                    .orElseThrow(() -> ApiException.badRequest("regionCode", "등록되지 않은 지역입니다."));
        }
        Optional<String> businessRegion = accessService.accessibleBusinesses(user).stream()
                .map(Business::regionCode).findFirst();
        if (businessRegion.isPresent()) {
            return regionDao.findByCode(businessRegion.get()).orElseThrow();
        }
        return regionDao.findAll().stream().findFirst()
                .orElseThrow(() -> ApiException.notFound("등록된 지역이 없습니다. 자료·기준 관리에서 지역을 먼저 확인해 주세요."));
    }

    private Map<String, Map<Integer, Integer>> valuesByRegion(long sourceId, String metricType, int year) {
        Map<String, Map<Integer, Integer>> result = new LinkedHashMap<>();
        for (RegionMonthlyStat s : monthlyDao.findBySourceAndPeriod(sourceId, metricType,
                YearMonth.of(year, 1).atDay(1), YearMonth.of(year, 12).atDay(1))) {
            result.computeIfAbsent(s.regionCode(), k -> new LinkedHashMap<>())
                    .put(s.statMonth().getMonthValue(), s.statValue());
        }
        return result;
    }

    private SourceInfo sourceInfo(long sourceId) {
        DataSourceFile s = sourceDao.findById(sourceId).orElseThrow();
        return new SourceInfo(s.sourceId(), s.fileName(), s.sourceVersion(), s.providerBasis(), s.unit(),
                VisitCsvValidator.UNIT_LABELS.getOrDefault(s.unit(), s.unit()), s.startMonth(), s.endMonth(),
                s.registeredAt());
    }

    private static String noDataMessage(int year, List<Integer> years) {
        if (years.isEmpty()) {
            return year + "년 자료가 없습니다. 아직 등록된 자료가 없어 그래프를 만들지 않습니다.";
        }
        return year + "년 자료가 없습니다. 조회할 수 있는 연도: "
                + years.stream().map(y -> y + "년").collect(Collectors.joining(", "));
    }

    static List<MonthValue> twelve(Map<Integer, Integer> values) {
        List<MonthValue> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(new MonthValue(m, values == null ? null : values.get(m)));
        }
        return months;
    }

    static MonthValue peak(List<MonthValue> months) {
        return months.stream().filter(m -> m.value() != null)
                .max(Comparator.comparingInt(MonthValue::value).thenComparing(MonthValue::month, Comparator.reverseOrder()))
                .orElse(null);
    }

    static MonthValue low(List<MonthValue> months) {
        return months.stream().filter(m -> m.value() != null)
                .min(Comparator.comparingInt(MonthValue::value).thenComparingInt(MonthValue::month))
                .orElse(null);
    }

    static BigDecimal changeRate(Integer from, Integer to) {
        if (from == null || to == null || from == 0) {
            return null;
        }
        return BigDecimal.valueOf(to - from).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(from), 1, RoundingMode.HALF_UP);
    }

    private static BigDecimal share(List<RegionConsumptionStat> list, String segment) {
        return list.stream().filter(s -> segment.equals(s.segment())).map(RegionConsumptionStat::shareRate)
                .filter(Objects::nonNull).findFirst().orElse(null);
    }
}
