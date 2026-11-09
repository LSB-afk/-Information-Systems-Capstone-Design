package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * DATA_VALIDATION_RESULT 테이블 한 행 (ERD: 자료 검증 결과), PK는 SOURCE_ID + VALIDATION_ITEM
 * VALIDATION_ITEM 코드값: PERIOD, AGGREGATION, REGION_CODE, MISSING_VALUE, PROVIDER (코드 설계서 6번)
 * RESULT 코드값: PASS, FAIL (코드 설계서 7번) — FAIL이면 REASON에 사유를 적음
 */
public record DataValidationResult(
        Long sourceId,
        String validationItem,
        String result,
        String reason,
        LocalDate validatedAt) {
}
