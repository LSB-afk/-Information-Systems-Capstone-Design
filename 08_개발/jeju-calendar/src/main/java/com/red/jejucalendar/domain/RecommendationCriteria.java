package com.red.jejucalendar.domain;

import java.time.LocalDate;

/** RECOMMENDATION_CRITERIA 테이블 한 행 (ERD: 추천 기준) */
public record RecommendationCriteria(
        Long criteriaId,
        Integer declineRateThreshold,
        String sourceInfo,
        String basePeriod,
        String reasonTemplate,
        String requiredCondition,
        LocalDate updatedAt) {
}
