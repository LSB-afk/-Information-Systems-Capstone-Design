package com.red.jejucalendar.domain;

/**
 * PROMO_SUGGESTION_ITEM 테이블 한 행 (ERD: 홍보 제안 항목), PK는 SUGGESTION_ID + ITEM_TYPE
 * ITEM_TYPE 코드값: TIMING, AUDIENCE, PRODUCT, ATTRACTION (코드 설계서 9번)
 */
public record PromoSuggestionItem(
        Long suggestionId,
        String itemType,
        String itemContent,
        String itemReason,
        String checkNote) {
}
