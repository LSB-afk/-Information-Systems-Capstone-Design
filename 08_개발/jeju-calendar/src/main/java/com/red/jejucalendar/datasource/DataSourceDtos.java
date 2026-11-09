package com.red.jejucalendar.datasource;

import java.time.LocalDate;
import java.util.List;

/** 자료·기준 관리 화면(SCR-A-001, SCR-A-001-P01)에서 주고받는 데이터 */
public final class DataSourceDtos {

    private DataSourceDtos() {
    }

    /**
     * CSV와 함께 보내는 자료 정보 (multipart 폼의 칸)
     * startMonth, endMonth는 2025-01 형식, unit은 PERSON_10K 또는 KRW_100M
     */
    public record SourceMeta(
            String sourceVersion,
            String startMonth,
            String endMonth,
            String unit,
            String providerBasis) {
    }

    /** 검증 항목 하나 (item은 코드 설계서 6번: PERIOD, AGGREGATION, REGION_CODE, MISSING_VALUE, PROVIDER) */
    public record CheckResult(String item, String label, boolean passed, String reason) {
    }

    /** SCR-A-001-P01 검증 결과 (registrable이 false면 등록 버튼을 비활성으로 둠) */
    public record ValidationResponse(
            String fileName,
            int rowCount,
            boolean registrable,
            String message,
            List<CheckResult> checks) {
    }

    public record RegisterResponse(long sourceId, int savedRows, String message, ValidationResponse validation) {
    }

    /** SCR-A-001 ⑬ 등록 자료 목록 */
    public record SourceItem(
            long sourceId,
            String fileName,
            String sourceVersion,
            LocalDate startMonth,
            LocalDate endMonth,
            String unit,
            String validationStatus,
            String providerBasis,
            int rowCount,
            LocalDate registeredAt) {
    }

    public record SourceListResponse(int count, List<SourceItem> sources) {
    }
}
