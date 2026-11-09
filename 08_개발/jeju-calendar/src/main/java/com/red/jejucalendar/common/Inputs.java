package com.red.jejucalendar.common;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 입력 검사 도우미
 * DB가 AL32UTF8 + BYTE 길이 기준이라 VARCHAR(n)에는 n바이트까지 들어감 (한글 1자 = 3바이트)
 */
public final class Inputs {

    /** 화면설계서 기준 숫자 입력 최댓값 (시안의 max="99999999") */
    public static final int MAX_COUNT = 99_999_999;

    private Inputs() {
    }

    /** 앞뒤 공백을 지운 값, 빈 문자열이면 null */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String required(String field, String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw ApiException.badRequest(field, message);
        }
        return trimmed;
    }

    /** 컬럼 바이트 길이 검사, 한글 기준 글자 수를 함께 안내함 */
    public static String maxBytes(String field, String label, String value, int maxBytes) {
        if (value != null && value.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            throw ApiException.badRequest(field,
                    label + "이(가) 너무 깁니다. 한글 기준 " + (maxBytes / 3) + "자까지 입력할 수 있어요.");
        }
        return value;
    }

    public static String code(String field, String value, Set<String> allowed, String message) {
        if (value == null || !allowed.contains(value)) {
            throw ApiException.badRequest(field, message);
        }
        return value;
    }

    /** 빈칸(null)은 미확보, 0은 실제로 확인한 0건 */
    public static Integer count(String field, String label, Integer value) {
        if (value != null && (value < 0 || value > MAX_COUNT)) {
            throw ApiException.badRequest(field, label + "은(는) 0부터 " + MAX_COUNT + "까지의 정수로 입력해 주세요.");
        }
        return value;
    }
}
