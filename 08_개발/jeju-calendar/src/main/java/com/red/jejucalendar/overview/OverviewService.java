package com.red.jejucalendar.overview;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.red.jejucalendar.analysis.AnalysisDtos.TrendResponse;
import com.red.jejucalendar.analysis.AnalysisService;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.dao.DataSourceFileDao;
import com.red.jejucalendar.dao.RecommendationCriteriaDao;
import com.red.jejucalendar.dao.RegionConsumptionStatDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.DataSourceFile;
import com.red.jejucalendar.plan.PlanDtos.CalendarResponse;
import com.red.jejucalendar.plan.PlanService;
import com.red.jejucalendar.suggestion.SuggestionDtos.ListResponse;
import com.red.jejucalendar.suggestion.SuggestionService;

/**
 * SCR-C-002 한눈에 보기: 지역 방문 흐름, 3개월 계획 요약, 홍보 제안 요약, 자료 준비 현황 (UC01·UC02·UC03·UC10·UC12)
 * 활용 사례(UC11)는 화면에 고정한 정적 예시라 DB와 이 API에 넣지 않음
 */
@Service
public class OverviewService {

    /** 자료 준비 현황 항목 하나 */
    public record StatusItem(String key, String label, boolean ready, String detail) {
    }

    /**
     * UC12 데이터 반영 현황
     * readyPercent는 네 항목(방문·소비·소비 비중 자료, 추천 기준) 중 준비된 항목의 비율이며 실자료 적용률이 아님
     */
    public record DataStatus(int sourceCount, int totalRows, int readyPercent, List<StatusItem> items, String notice) {
    }

    public record OverviewResponse(
            Long businessId,
            String businessName,
            TrendResponse trend,
            ListResponse suggestions,
            CalendarResponse calendar,
            DataStatus dataStatus) {
    }

    private final AnalysisService analysisService;
    private final SuggestionService suggestionService;
    private final PlanService planService;
    private final BusinessAccessService accessService;
    private final DataSourceFileDao sourceDao;
    private final RegionMonthlyStatDao monthlyDao;
    private final RegionConsumptionStatDao consumptionDao;
    private final RecommendationCriteriaDao criteriaDao;

    public OverviewService(AnalysisService analysisService, SuggestionService suggestionService,
            PlanService planService, BusinessAccessService accessService, DataSourceFileDao sourceDao,
            RegionMonthlyStatDao monthlyDao, RegionConsumptionStatDao consumptionDao,
            RecommendationCriteriaDao criteriaDao) {
        this.analysisService = analysisService;
        this.suggestionService = suggestionService;
        this.planService = planService;
        this.accessService = accessService;
        this.sourceDao = sourceDao;
        this.monthlyDao = monthlyDao;
        this.consumptionDao = consumptionDao;
        this.criteriaDao = criteriaDao;
    }

    /** ①②③: 선택 지역 기준으로 분석·제안을 묶고, 처리할 수 있는 사업체가 있으면 계획 요약을 함께 줌 */
    public OverviewResponse overview(LoginUser user, String regionCode, Long businessId, String metric) {
        Optional<Business> business = businessId != null
                ? Optional.of(accessService.requireAccess(user, businessId))
                : accessService.accessibleBusinesses(user).stream().findFirst();
        String region = regionCode != null && !regionCode.isBlank() ? regionCode
                : business.map(Business::regionCode).orElse(null);
        TrendResponse trend = analysisService.trend(user, region, null, metric);
        ListResponse suggestions = suggestionService.list(user, trend.regionCode(),
                business.map(Business::businessId).orElse(null));
        CalendarResponse calendar = business.map(b -> planService.calendar(user, b.businessId())).orElse(null);
        return new OverviewResponse(business.map(Business::businessId).orElse(null),
                business.map(Business::businessName).orElse(null), trend, suggestions, calendar, dataStatus());
    }

    /** SCR-C-002 사례와 자료 현황 ③④ */
    public DataStatus dataStatus() {
        List<DataSourceFile> sources = sourceDao.findByValidationStatus("PASS");
        int rows = sources.stream().mapToInt(s -> s.rowCount() == null ? 0 : s.rowCount()).sum();
        boolean visit = !monthlyDao.findYears("VISIT").isEmpty();
        boolean spend = !monthlyDao.findYears("SPEND").isEmpty();
        int shareRows = consumptionDao.countPassed();
        boolean criteria = criteriaDao.findCurrent().isPresent();
        List<StatusItem> items = List.of(
                new StatusItem("VISIT", "지역 방문 자료", visit, visit ? "검증 통과 자료 등록됨" : "미등록"),
                new StatusItem("SPEND", "카드 소비 자료", spend, spend ? "검증 통과 자료 등록됨" : "미등록"),
                new StatusItem("SHARE", "업종·내외국인·시간대 소비 비중", shareRows > 0,
                        shareRows > 0 ? shareRows + "행" : "미등록"),
                new StatusItem("CRITERIA", "추천 기준", criteria, criteria ? "적용 중" : "미설정"));
        int ready = (int) items.stream().filter(StatusItem::ready).count();
        return new DataStatus(sources.size(), rows, ready * 100 / items.size(), items,
                "준비 비율은 추천 기준을 포함한 항목 상태의 비율이며 실자료 적용률이 아닙니다.");
    }
}
