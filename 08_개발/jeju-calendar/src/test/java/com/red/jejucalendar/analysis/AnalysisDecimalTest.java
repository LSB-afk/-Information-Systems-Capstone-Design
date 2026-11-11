package com.red.jejucalendar.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** 단위 변환 뒤 소수 부분을 버리면 순위·변화율이 달라지는 회귀를 검사함 */
class AnalysisDecimalTest {

    @Test
    void fractionalValuesKeepTheirOrderingAndMissingMonths() {
        var months = AnalysisService.twelve(Map.of(
                1, new BigDecimal("49.3209"),
                2, new BigDecimal("49.3208"),
                3, BigDecimal.ZERO));

        assertThat(months).hasSize(12);
        assertThat(months.get(0).value()).isEqualByComparingTo("49.3209");
        assertThat(months.get(3).value()).isNull();
        assertThat(AnalysisService.peak(months).month()).isEqualTo(1);
        assertThat(AnalysisService.low(months).month()).isEqualTo(3);
    }

    @Test
    void changeRateUsesFractionalValuesAndKeepsUnknownResults() {
        assertThat(AnalysisService.changeRate(new BigDecimal("0.493209"), new BigDecimal("0.481802")))
                .isEqualByComparingTo("-2.3");
        assertThat(AnalysisService.changeRate(null, BigDecimal.ONE)).isNull();
        assertThat(AnalysisService.changeRate(BigDecimal.ZERO, BigDecimal.ONE)).isNull();
    }
}
