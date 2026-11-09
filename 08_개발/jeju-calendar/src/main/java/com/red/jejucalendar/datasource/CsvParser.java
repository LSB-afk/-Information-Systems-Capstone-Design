package com.red.jejucalendar.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * 따옴표를 지원하는 간단한 CSV 읽기 (화면 시안 app.js의 parseCSV와 같은 규칙)
 * 셀 앞뒤 공백은 지우고, 빈 줄은 건너뜀
 */
final class CsvParser {

    private CsvParser() {
    }

    static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                row.add(value.toString().strip());
                value.setLength(0);
            } else if ((c == '\n' || c == '\r') && !quoted) {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(value.toString().strip());
                value.setLength(0);
                addIfNotBlank(rows, row);
                row = new ArrayList<>();
            } else {
                value.append(c);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("닫히지 않은 따옴표가 있습니다.");
        }
        row.add(value.toString().strip());
        addIfNotBlank(rows, row);
        return rows;
    }

    private static void addIfNotBlank(List<List<String>> rows, List<String> row) {
        if (row.stream().anyMatch(v -> !v.isEmpty())) {
            rows.add(row);
        }
    }
}
