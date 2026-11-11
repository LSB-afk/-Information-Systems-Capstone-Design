package com.red.jejucalendar.suggestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** 제안 판단 규칙 확인 (DB를 쓰지 않음) */
class SuggestionRulesTest {

    /** 화면 시안의 애월읍 2025년 예시 방문 값 */
    static final int[] AEWOL = {35, 32, 44, 49, 55, 47, 65, 72, 57, 51, 37, 34};
    static final List<YearMonth> TARGETS = List.of(YearMonth.of(2026, 10), YearMonth.of(2026, 11), YearMonth.of(2026, 12));

    static Map<Integer, Integer> values(int[] v) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < v.length; i++) {
            map.put(i + 1, v[i]);
        }
        return map;
    }

    @Test
    void fractionalVisitorCountsDoNotDisappearBeforeComparingTheThreshold() {
        var decisions = SuggestionRules.decide(20, 2025,
                Map.of(10, new BigDecimal("0.5001"), 11, new BigDecimal("0.4000")),
                List.of(YearMonth.of(2026, 11)));

        assertThat(decisions).hasSize(1);
        assertThat(decisions.get(0).previousValue()).isEqualByComparingTo("0.5001");
        assertThat(decisions.get(0).currentValue()).isEqualByComparingTo("0.4000");
        assertThat(decisions.get(0).declineRate()).isEqualByComparingTo("20.0");
    }

    @Test
    void sameResultAsThePrototypeWithThreshold20() {
        var decisions = SuggestionRules.decide(20, 2025, values(AEWOL), TARGETS);
        // 9월→10월 10.5%, 10월→11월 27.5%, 11월→12월 8.1% → 11월만 기준 이상
        assertThat(decisions).hasSize(1);
        var d = decisions.get(0);
        assertThat(d.target()).isEqualTo(YearMonth.of(2026, 11));
        assertThat(d.previousValue()).isEqualByComparingTo("51");
        assertThat(d.currentValue()).isEqualByComparingTo("37");
        assertThat(d.declineRate()).isEqualByComparingTo("27.5");
        assertThat(SuggestionRules.reason(d, 20)).isEqualTo("2025년 10월 대비 11월 방문 27.5% 감소 · 기준 20% 이상");
    }

    @Test
    void lowerThresholdAddsMonthsAndEqualValueCounts() {
        assertThat(SuggestionRules.decide(10, 2025, values(AEWOL), TARGETS))
                .extracting(d -> d.target().getMonthValue()).containsExactly(10, 11);
        // 51 → 37은 27.45…%를 반올림한 27.5%라 기준 28%보다 작아 제외됨
        assertThat(SuggestionRules.decide(28, 2025, values(AEWOL), TARGETS)).isEmpty();
        // 50 → 40은 정확히 20.0%라 기준 20%와 같으면 포함됨
        assertThat(SuggestionRules.decide(20, 2025, Map.of(10, 50, 11, 40), List.of(YearMonth.of(2026, 11))))
                .hasSize(1);
        // 세 달 모두 방문이 줄어 기준 0%면 세 달 모두 제안
        assertThat(SuggestionRules.decide(0, 2025, values(AEWOL), TARGETS)).hasSize(3);
    }

    @Test
    void missingValuesAndJanuaryAreNotJudged() {
        Map<Integer, Integer> partial = values(AEWOL);
        partial.remove(10);
        // 10월 값이 없으면 9월→10월, 10월→11월은 판단하지 않고 11월→12월만 판단함
        assertThat(SuggestionRules.decide(5, 2025, partial, TARGETS))
                .extracting(d -> d.target().getMonthValue()).containsExactly(12);
        assertThat(SuggestionRules.decide(0, 2025, values(AEWOL), List.of(YearMonth.of(2027, 1)))).isEmpty();
        Map<Integer, Integer> zero = values(AEWOL);
        zero.put(9, 0);
        assertThat(SuggestionRules.decide(0, 2025, zero, List.of(YearMonth.of(2026, 10)))).isEmpty();
    }

    @Test
    void templatesFitTheDbColumnLengths() {
        for (int month = 1; month <= 12; month++) {
            var t = SuggestionRules.template(month);
            assertThat(bytes(t.title())).as("TITLE %d월", month).isLessThanOrEqualTo(100);
            assertThat(bytes(t.description())).as("DESCRIPTION").isLessThanOrEqualTo(200);
            assertThat(bytes(t.tag())).as("TAG").isLessThanOrEqualTo(50);
            assertThat(bytes(t.task())).as("PREPARATION_TASK").isLessThanOrEqualTo(500);
            assertThat(bytes(t.product())).as("ITEM_CONTENT").isLessThanOrEqualTo(200);
            assertThat(bytes(t.productReason())).as("ITEM_REASON").isLessThanOrEqualTo(200);
        }
        var longest = new SuggestionRules.Decision(YearMonth.of(2026, 12), 2025, 11,
                BigDecimal.valueOf(99_999_999), BigDecimal.ZERO, new BigDecimal("100.0"));
        assertThat(bytes(SuggestionRules.reason(longest, 100))).isLessThanOrEqualTo(200);
    }

    static int bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }
}
