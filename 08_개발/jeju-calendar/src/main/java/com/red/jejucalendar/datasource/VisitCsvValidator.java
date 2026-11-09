package com.red.jejucalendar.datasource;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.red.jejucalendar.datasource.DataSourceDtos.CheckResult;
import com.red.jejucalendar.datasource.DataSourceDtos.SourceMeta;

/**
 * 지역 월별 집계 CSV의 다섯 가지 검증 (UC08, 화면 시안 app.js validateCSV 기준)
 * DB를 쓰지 않는 순수 계산이라 단위 테스트로 확인함
 * 없는 수치를 만들어 채우거나 0으로 바꾸지 않음
 */
public final class VisitCsvValidator {

    public static final List<String> HEADER = List.of("region_code", "month", "value", "unit", "provider", "aggregation");
    public static final String AGGREGATION = "월별 지역 집계";
    /** 단위 코드(코드 설계서 11번)와 CSV에 적는 단위 이름 */
    public static final Map<String, String> UNIT_LABELS = Map.of("PERSON_10K", "만 명", "KRW_100M", "억 원");

    /** CSV 한 행 (검증을 통과한 뒤 REGION_MONTHLY_STAT에 저장할 값) */
    public record Row(String regionCode, YearMonth month, Integer value) {
    }

    /** parsedRowCount는 머리글을 뺀 CSV 행 수, rows는 모두 통과했을 때만 채움 */
    public record Result(List<CheckResult> checks, List<Row> rows, String parseError, int parsedRowCount) {
        public boolean passed() {
            return parseError == null && checks.stream().allMatch(CheckResult::passed);
        }
    }

    private VisitCsvValidator() {
    }

    public static Result validate(String csvText, SourceMeta meta, Set<String> knownRegionCodes) {
        List<List<String>> table;
        String parseError = null;
        try {
            table = readTable(csvText);
        } catch (IllegalArgumentException e) {
            table = List.of();
            parseError = e.getMessage();
        }
        boolean exists = !table.isEmpty();

        YearMonth from = month(meta.startMonth());
        YearMonth to = month(meta.endMonth());
        String unitLabel = meta.unit() == null ? null : UNIT_LABELS.get(meta.unit());
        String provider = meta.providerBasis() == null ? "" : meta.providerBasis().strip();
        String version = meta.sourceVersion() == null ? "" : meta.sourceVersion().strip();

        boolean periodOk = exists && from != null && to != null && !from.isAfter(to)
                && table.stream().allMatch(r -> {
                    YearMonth m = month(r.get(1));
                    return m != null && !m.isBefore(from) && !m.isAfter(to);
                });

        Set<String> seen = new HashSet<>();
        boolean noDuplicates = table.stream().allMatch(r -> seen.add(r.get(0) + "|" + r.get(1)));
        boolean aggregationOk = exists && unitLabel != null && noDuplicates
                && table.stream().allMatch(r -> AGGREGATION.equals(r.get(5)) && unitLabel.equals(r.get(3)));

        boolean regionOk = exists && table.stream().allMatch(r -> knownRegionCodes.contains(r.get(0)));

        boolean missingOk = exists && !version.isEmpty()
                && table.stream().allMatch(r -> r.stream().noneMatch(String::isEmpty) && integer(r.get(2)) != null);

        boolean providerOk = exists && !provider.isEmpty() && table.stream().allMatch(r -> provider.equals(r.get(4)));

        String formatReason = parseError != null ? "CSV 형식 오류 · " + parseError : null;
        List<CheckResult> checks = List.of(
                check("PERIOD", "자료 기간", periodOk, formatReason,
                        "기준기간의 시작·종료 순서가 맞지 않거나 기준기간 밖의 월, 월 형식(2025-01) 불일치"),
                check("AGGREGATION", "집계 기준", aggregationOk, formatReason,
                        "월별 지역 집계·선택 단위 불일치 또는 같은 지역·월이 두 번 있음"),
                check("REGION_CODE", "지역 코드", regionOk, formatReason,
                        "등록된 지역(REGION)에 없는 지역 코드"),
                check("MISSING_VALUE", "누락값", missingOk, formatReason,
                        "출처·버전 또는 필수 값 누락, 음수·소수·숫자 형식 오류 · 0으로 대체하지 않음"),
                check("PROVIDER", "제공 기준 일관성", providerOk, formatReason,
                        "입력한 제공자·집계 기준과 CSV의 provider가 다름"));

        List<Row> rows = new ArrayList<>();
        if (parseError == null && checks.stream().allMatch(CheckResult::passed)) {
            table.forEach(r -> rows.add(new Row(r.get(0), month(r.get(1)), integer(r.get(2)))));
        }
        return new Result(checks, rows, parseError, table.size());
    }

    /** 머리글을 확인하고 자료 행만 돌려줌 */
    private static List<List<String>> readTable(String csvText) {
        if (csvText == null || csvText.isBlank()) {
            throw new IllegalArgumentException("먼저 CSV 파일을 선택해 주세요.");
        }
        List<List<String>> rows = new ArrayList<>(CsvParser.parse(csvText.replaceFirst("^﻿", "")));
        List<String> header = rows.isEmpty() ? List.of() : rows.remove(0);
        if (!HEADER.equals(header)) {
            throw new IllegalArgumentException("필수 열 6개의 이름과 순서를 확인해 주세요. (" + String.join(",", HEADER) + ")");
        }
        if (rows.isEmpty() || rows.stream().anyMatch(r -> r.size() != HEADER.size())) {
            throw new IllegalArgumentException("자료 행이 없거나 열 개수가 다릅니다.");
        }
        return rows;
    }

    private static CheckResult check(String item, String label, boolean ok, String formatReason, String reason) {
        if (ok) {
            return new CheckResult(item, label, true, null);
        }
        return new CheckResult(item, label, false, formatReason != null ? formatReason : reason);
    }

    static YearMonth month(String value) {
        if (value == null || !value.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            return null;
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** STAT_VALUE가 INTEGER라 0 이상의 정수만 받음 */
    static Integer integer(String value) {
        if (value == null || !value.matches("\\d{1,9}")) {
            return null;
        }
        return Integer.valueOf(value);
    }
}
