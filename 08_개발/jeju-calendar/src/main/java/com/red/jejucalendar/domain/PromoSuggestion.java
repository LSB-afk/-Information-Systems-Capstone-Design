package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * PROMO_SUGGESTION 테이블 한 행 (ERD: 홍보 제안)
 * TARGET_MONTH는 매월 1일로 저장함
 * CHANNEL 코드값: INSTAGRAM, STORE, BLOG, ETC (코드 설계서 4번)
 */
public record PromoSuggestion(
        Long suggestionId,
        Long criteriaId,
        LocalDate targetMonth,
        String title,
        String reason,
        String regionCode,
        String description,
        LocalDate recommendedDate,
        String channel,
        String preparationTask,
        String tag,
        Long sourceId) {
}
