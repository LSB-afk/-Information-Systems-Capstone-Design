package com.red.jejucalendar.suggestion;

import java.time.LocalDate;
import java.util.List;

import com.red.jejucalendar.analysis.AnalysisDtos.MonthValue;
import com.red.jejucalendar.analysis.AnalysisDtos.SourceInfo;

/** 홍보 제안 화면(SCR-C-004, SCR-C-004-P01)에서 주고받는 데이터 */
public final class SuggestionDtos {

    private SuggestionDtos() {
    }

    /** 적용 중인 추천 기준 요약 (출처·판단 이유·적용 조건은 제안과 함께 표시) */
    public record CriteriaSummary(
            long criteriaId,
            int declineRateThreshold,
            String sourceInfo,
            String basePeriod,
            String reasonTemplate,
            String requiredCondition,
            LocalDate updatedAt) {
    }

    /** 제안 항목 (TIMING 시기, AUDIENCE 대상, PRODUCT 상품 후보, ATTRACTION 주변 관광지) */
    public record Item(String itemType, String content, String reason, String checkNote) {
    }

    /** 제안 카드 (addedPlanId가 있으면 이미 일정에 담은 제안이라 그 계획 수정으로 연결) */
    public record Card(
            long suggestionId,
            String regionCode,
            LocalDate targetMonth,
            String title,
            String description,
            String tag,
            String reason,
            String channel,
            LocalDate recommendedDate,
            String preparationTask,
            List<Item> items,
            SourceInfo source,
            Long addedPlanId) {
    }

    public record MonthSuggestions(String month, List<Card> suggestions) {
    }

    /** SCR-C-004 월별 목록 */
    public record ListResponse(
            String regionCode,
            String regionName,
            String periodLabel,
            CriteriaSummary criteria,
            Long businessId,
            boolean canAddToPlan,
            int totalCount,
            String message,
            List<MonthSuggestions> months) {
    }

    /** SCR-C-004-P01 추천 근거 상세 */
    public record Detail(
            Card card,
            CriteriaSummary criteria,
            int baseYear,
            List<MonthValue> observed,
            List<String> confirmedConditions,
            List<String> uncheckedConditions,
            boolean canAddToPlan,
            String notice) {
    }

    /** 다시 계산한 결과 */
    public record RecalcResult(int created, int updated, int deleted, String message) {
    }
}
