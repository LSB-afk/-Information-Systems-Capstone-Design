package com.red.jejucalendar.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.red.jejucalendar.datasource.DataSourceDtos.CheckResult;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceMeta;

/** 다섯 검증 규칙 확인 (DB를 쓰지 않음) */
class VisitCsvValidatorTest {

    static final Set<String> REGIONS = Set.of("50110253", "50110256");
    static final SourceMeta META = new SourceMeta("방문 자료 v1", "2025-01", "2025-12", "PERSON_10K", "제주관광공사");
    static final String HEADER = "region_code,month,value,unit,provider,aggregation\n";

    static String row(String region, String month, String value, String unit, String provider, String aggregation) {
        return String.join(",", region, month, value, unit, provider, aggregation) + "\n";
    }

    static String ok(String region, String month, String value) {
        return row(region, month, value, "만 명", "제주관광공사", "월별 지역 집계");
    }

    static Map<String, Boolean> results(VisitCsvValidator.Result r) {
        return r.checks().stream().collect(Collectors.toMap(CheckResult::item, CheckResult::passed));
    }

    @Test
    void validFilePassesAllFiveAndKeepsRows() {
        var r = VisitCsvValidator.validate("﻿" + HEADER + ok("50110253", "2025-01", "35") + ok("50110256", "2025-12", "0"),
                META, REGIONS);
        assertThat(r.passed()).isTrue();
        assertThat(r.checks()).hasSize(5);
        assertThat(r.rows()).containsExactly(
                new VisitCsvValidator.Row("50110253", YearMonth.of(2025, 1), 35),
                new VisitCsvValidator.Row("50110256", YearMonth.of(2025, 12), 0));
    }

    @Test
    void eachRuleFailsOnItsOwn() {
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2026-01", "35"), META, REGIONS)))
                .containsEntry("PERIOD", false).containsEntry("AGGREGATION", true);
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2025-1", "35"), META, REGIONS)))
                .containsEntry("PERIOD", false);
        assertThat(results(VisitCsvValidator.validate(HEADER
                + row("50110253", "2025-01", "35", "억 원", "제주관광공사", "월별 지역 집계"), META, REGIONS)))
                .containsEntry("AGGREGATION", false);
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2025-01", "35") + ok("50110253", "2025-01", "36"),
                META, REGIONS))).containsEntry("AGGREGATION", false);
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("99999999", "2025-01", "35"), META, REGIONS)))
                .containsEntry("REGION_CODE", false).containsEntry("PERIOD", true);
        assertThat(results(VisitCsvValidator.validate(HEADER
                + row("50110253", "2025-01", "35", "만 명", "다른 기관", "월별 지역 집계"), META, REGIONS)))
                .containsEntry("PROVIDER", false).containsEntry("MISSING_VALUE", true);
    }

    @Test
    void missingNegativeOrDecimalValuesAreNotReplacedWithZero() {
        for (String bad : new String[] {"", "-3", "35.5", "약 30"}) {
            var r = VisitCsvValidator.validate(HEADER + ok("50110253", "2025-01", bad), META, REGIONS);
            assertThat(results(r)).as(bad).containsEntry("MISSING_VALUE", false);
            assertThat(r.rows()).isEmpty();
        }
        var noVersion = new SourceMeta(" ", "2025-01", "2025-12", "PERSON_10K", "제주관광공사");
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2025-01", "35"), noVersion, REGIONS)))
                .containsEntry("MISSING_VALUE", false);
    }

    @Test
    void metadataErrorsFailTheRelatedItem() {
        var reversed = new SourceMeta("v1", "2025-12", "2025-01", "PERSON_10K", "제주관광공사");
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2025-06", "35"), reversed, REGIONS)))
                .containsEntry("PERIOD", false);
        var badUnit = new SourceMeta("v1", "2025-01", "2025-12", "PERSON", "제주관광공사");
        assertThat(results(VisitCsvValidator.validate(HEADER + ok("50110253", "2025-06", "35"), badUnit, REGIONS)))
                .containsEntry("AGGREGATION", false);
    }

    @Test
    void formatErrorsFailEveryItemWithTheReason() {
        var wrongHeader = VisitCsvValidator.validate("region,month\n50110253,2025-01\n", META, REGIONS);
        assertThat(wrongHeader.passed()).isFalse();
        assertThat(wrongHeader.parseError()).contains("필수 열 6개");
        assertThat(wrongHeader.checks()).allSatisfy(c -> {
            assertThat(c.passed()).isFalse();
            assertThat(c.reason()).startsWith("CSV 형식 오류");
        });
        assertThat(VisitCsvValidator.validate(HEADER, META, REGIONS).parseError()).contains("자료 행이 없거나");
        assertThat(VisitCsvValidator.validate(HEADER + "50110253,\"2025-01,35\n", META, REGIONS).parseError())
                .contains("따옴표");
    }

    @Test
    void quotedCellsAreRead() {
        var r = VisitCsvValidator.validate(HEADER
                + "\"50110253\",2025-03,\"40\",만 명,\"제주관광공사\",월별 지역 집계\r\n", META, REGIONS);
        assertThat(r.passed()).isTrue();
        assertThat(r.rows().get(0).value()).isEqualTo(40);
    }
}
