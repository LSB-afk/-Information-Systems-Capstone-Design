package com.red.jejucalendar.domain;

/** REGION 테이블 한 행 (ERD: 지역) — 지역은 코드가 아니라 기준 데이터로 관리함 */
public record Region(
        String regionCode,
        String regionName) {
}
