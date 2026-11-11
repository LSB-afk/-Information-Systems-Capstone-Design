package com.red.jejucalendar.analysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.red.jejucalendar.datasource.VisitCsvValidator;
import com.red.jejucalendar.domain.DataSourceFile;

/**
 * 지역 분석 화면(SCR-C-003)에서 주고받는 데이터
 * 값이 null이면 미확보(미수집)이며 0으로 바꾸지 않음
 */
public final class AnalysisDtos {

    private AnalysisDtos() {
    }

    /** UC10 자료 출처·기준 표시 */
    public record SourceInfo(
            long sourceId,
            String fileName,
            String sourceVersion,
            String providerBasis,
            String unit,
            String unitLabel,
            LocalDate startMonth,
            LocalDate endMonth,
            LocalDate registeredAt) {

        public static SourceInfo of(DataSourceFile s) {
            return new SourceInfo(s.sourceId(), s.fileName(), s.sourceVersion(), s.providerBasis(), s.unit(),
                    VisitCsvValidator.UNIT_LABELS.getOrDefault(s.unit(), s.unit()), s.startMonth(), s.endMonth(),
                    s.registeredAt());
        }
    }

    public record MonthValue(int month, BigDecimal value) {
    }

    /** 지도·지역 선택용 (value는 계획 시작 월의 값) */
    public record RegionValue(String regionCode, String regionName, BigDecimal value) {
    }

    /**
     * 성수기·비수기 비교 표 한 줄 (SCR-C-003 ⑨)
     * changeRate = (다음 달 − 시작 달) ÷ 시작 달 × 100, 소수 첫째 자리 반올림 값이 없거나 시작 달이 0이면 null
     */
    public record SeasonRow(
            String regionCode,
            String regionName,
            MonthValue peak,
            MonthValue low,
            BigDecimal changeRate) {
    }

    /** SCR-C-003 방문·계절 비교 (metric: VISIT 방문 만 명, SPEND 소비 억 원) */
    public record TrendResponse(
            String regionCode,
            String regionName,
            int year,
            String metric,
            String unitLabel,
            List<Integer> availableYears,
            boolean hasData,
            String message,
            SourceInfo source,
            List<MonthValue> months,
            MonthValue peak,
            MonthValue low,
            List<RegionValue> regions,
            String changeLabel,
            List<SeasonRow> seasonTable,
            String notice) {
    }

    /** 방문·소비를 같은 달끼리 나란히 보여주는 줄 (SCR-C-003 소비 비교 ③) */
    public record VisitSpendRow(int month, BigDecimal visit, BigDecimal spend, String note) {
    }

    /** 업종별 내국인·외국인 비중 (SCR-C-003 소비 비교 ④) */
    public record SegmentRow(String category, BigDecimal domestic, BigDecimal foreign, boolean comparable, String note) {
    }

    /** 시간대별 비중 (SCR-C-003 소비 비교 ⑤) */
    public record TimeSlotRow(String timeSlot, String label, BigDecimal share) {
    }

    public record ShareSection(
            boolean hasData,
            String message,
            SourceInfo source,
            List<SegmentRow> segments,
            String timeSlotCategory,
            List<TimeSlotRow> timeSlots) {
    }

    public record ConsumptionResponse(
            String regionCode,
            String regionName,
            int year,
            int shareMonth,
            List<Integer> availableYears,
            SourceInfo visitSource,
            SourceInfo spendSource,
            List<VisitSpendRow> visitSpend,
            ShareSection shares,
            String notice) {
    }
}
