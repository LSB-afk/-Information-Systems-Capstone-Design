package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * EXECUTION_RECORD 테이블 한 행 (ERD: 실행 결과 기록) — 계획 하나에 기록 하나 (AK: PLAN_ID)
 * EXEC_STATUS 코드값: DONE, NOT_DONE, CHECKING (코드 설계서 2번)
 * ADOPTION_STATUS 코드값: YES, NO, UNKNOWN (코드 설계서 3번)
 */
public record ExecutionRecord(
        Long recordId,
        Long planId,
        String execStatus,
        LocalDate executedDate,
        Integer checkedClickCount,
        Integer checkedBookingCount,
        Integer couponUsageCount,
        String resultMemo,
        String collectionMethod,
        String adoptionStatus,
        String actualActivity,
        Integer spentTimeMin) {
}
