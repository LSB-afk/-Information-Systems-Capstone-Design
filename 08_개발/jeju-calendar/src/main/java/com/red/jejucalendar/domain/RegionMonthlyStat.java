package com.red.jejucalendar.domain;

import java.time.LocalDate;

/**
 * REGION_MONTHLY_STAT 테이블 한 행 (ERD: 지역 월별 통계)
 * AK: SOURCE_ID + REGION_CODE + STAT_MONTH + METRIC_TYPE, STAT_MONTH는 매월 1일로 저장함
 * METRIC_TYPE 코드값: VISIT(방문, 만 명), SPEND(소비, 억 원) (코드 설계서 5번)
 * 지역 방문객 집계이며 개별 가게의 실제 손님 수가 아님
 */
public record RegionMonthlyStat(
        Long statId,
        Long sourceId,
        String regionCode,
        LocalDate statMonth,
        String metricType,
        Integer statValue) {
}
