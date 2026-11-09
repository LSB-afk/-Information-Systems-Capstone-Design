package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * USERS 테이블 한 행 (ERD: 사용자)
 * ROLE 코드값: BUSINESS(사업체 운영자), SERVICE(서비스 운영자) — ERD 코드 설계서 1번
 */
public record User(
        Long userId,
        String email,
        String passwordHash,
        String role,
        LocalDate createdAt) {
}
