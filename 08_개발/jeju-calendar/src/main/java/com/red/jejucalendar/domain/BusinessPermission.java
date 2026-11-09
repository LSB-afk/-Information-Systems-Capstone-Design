package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * BUSINESS_PERMISSION 테이블 한 행 (ERD: 사업체 권한), PK는 USER_ID + BUSINESS_ID
 * PERMISSION_TYPE 코드값: OWNER, MANAGER — ERD 코드 설계서 10번
 */
public record BusinessPermission(
        Long userId,
        Long businessId,
        String permissionType,
        LocalDate grantedAt) {
}
