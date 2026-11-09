package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * DATA_SOURCE 테이블 한 행 (ERD: 자료) — 등록한 CSV 파일 하나
 * 이름이 javax.sql.DataSource와 겹치지 않도록 클래스 이름은 DataSourceFile로 함
 * START_MONTH, END_MONTH는 매월 1일로 저장함
 * UNIT 코드값: PERSON_10K, KRW_100M (코드 설계서 11번)
 * VALIDATION_STATUS 코드값: PASS, FAIL, PENDING (코드 설계서 8번)
 */
public record DataSourceFile(
        Long sourceId,
        Long userId,
        String fileName,
        String sourceVersion,
        LocalDate startMonth,
        LocalDate endMonth,
        String unit,
        String validationStatus,
        String providerBasis,
        Integer rowCount,
        LocalDate registeredAt) {
}
