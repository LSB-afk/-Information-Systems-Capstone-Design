package com.red.jejucalendar.domain;

/**
 * BUSINESS 테이블 한 행 (ERD: 사업체)
 * CATEGORY 코드값: CAFE, EXPERIENCE, RESTAURANT — ERD 코드 설계서 14번
 */
public record Business(
        Long businessId,
        Long userId,
        String businessName,
        String category,
        String regionCode) {
}
