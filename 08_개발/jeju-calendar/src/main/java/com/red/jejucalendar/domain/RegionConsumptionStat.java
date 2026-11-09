package com.red.jejucalendar.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REGION_CONSUMPTION_STAT 테이블 한 행 (ERD: 지역 소비 통계)
 * AK: SOURCE_ID + REGION_CODE + STAT_MONTH + CATEGORY + SEGMENT + TIME_SLOT, STAT_MONTH는 매월 1일로 저장함
 * CATEGORY: CAFE, EXPERIENCE, RESTAURANT (코드 설계서 14번)
 * SEGMENT: DOMESTIC, FOREIGN, ALL (12번) / TIME_SLOT: T0611, T1114, T1418, T1806, ALL (13번)
 * SHARE_RATE는 비중(%) NUMBER(5,2)
 */
public record RegionConsumptionStat(
        Long consumptionStatId,
        Long sourceId,
        String regionCode,
        LocalDate statMonth,
        String category,
        String segment,
        String timeSlot,
        BigDecimal shareRate) {
}
