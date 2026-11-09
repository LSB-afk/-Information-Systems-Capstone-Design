package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * PROMO_PLAN 테이블 한 행 (ERD: 홍보 계획)
 * SUGGESTION_ID는 제안 없이 직접 추가한 계획이면 NULL
 * CHANNEL 코드값: INSTAGRAM, STORE, BLOG, ETC (코드 설계서 4번)
 */
public record PromoPlan(
        Long planId,
        Long businessId,
        Long suggestionId,
        String title,
        LocalDate scheduledDate,
        String channel,
        String preparationNote) {
}
