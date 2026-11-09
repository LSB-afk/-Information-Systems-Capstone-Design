package com.red.jejucalendar.suggestion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 홍보 제안을 만들지 판단하는 규칙과 제안 문구 (화면 시안 app.js visibleRecommendations 기준)
 * 계획 대상 월(예: 2026년 11월)과 같은 달의 과거 자료(예: 2025년 11월)를 직전 달(2025년 10월)과 비교해
 * 방문 감소율이 추천 기준(%) 이상이면 그 달의 제안을 만듦
 * DB를 쓰지 않는 계산이라 단위 테스트로 확인함
 */
public final class SuggestionRules {

    private SuggestionRules() {
    }

    /** 제안 문구 틀 (DB 길이 기준: 제목 100바이트, 설명 200바이트, 태그 50바이트 안에 들어감) */
    public record Template(String title, String description, String tag, String channel, int day, String task,
            String product, String productReason) {
    }

    /** 판단 결과 하나 (기준을 넘은 달만 만듦) */
    public record Decision(YearMonth target, int baseYear, int previousMonth, int previousValue, int currentValue,
            BigDecimal declineRate) {
    }

    private static final Map<Integer, Template> TEMPLATES = Map.of(
            10, new Template("가을 산책길, 우리 가게 알리기",
                    "방문이 이어지는 가을에 대표 메뉴와 가게 위치를 소개해 보세요.", "방문 계기 만들기", "INSTAGRAM", 19,
                    "대표 메뉴 사진 3장과 찾아오는 길 안내를 준비합니다.",
                    "대표 계절 음료와 재방문 안내", "계절 흐름을 고려한 후보"),
            11, new Template("따뜻한 음료로 채우는 평일 오후",
                    "실내에서 머물 이유를 제안하세요. 판매 가능한 음료 세트부터 확인합니다.", "비수기 준비", "STORE", 16,
                    "판매 가능한 음료·디저트 구성을 정하고 매장 안내물을 만듭니다.",
                    "따뜻한 음료·디저트 세트", "계절과 실내 이용을 고려한 후보"),
            12, new Template("다시 찾고 싶은 연말의 작은 혜택",
                    "다음 방문에 쓸 혜택을 설계하고 비용과 운영 가능 여부를 확인하세요.", "재방문 제안", "STORE", 7,
                    "운영 가능한 혜택과 이용 조건을 정하고 안내합니다.",
                    "연말 재방문 혜택", "재방문을 유도하는 후보 · 효과는 확인되지 않음"));

    private static final Template DEFAULT_TEMPLATE = new Template("방문이 줄어드는 달, 단골에게 알리기",
            "방문 감소에 대비해 단골 고객에게 가게 소식을 알리세요.", "비수기 준비", "INSTAGRAM", 15,
            "알릴 소식과 채널을 정하고 안내 문구를 준비합니다.",
            "대표 메뉴 안내", "방문 감소 시기에 알릴 수 있는 후보");

    public static Template template(int month) {
        return TEMPLATES.getOrDefault(month, DEFAULT_TEMPLATE);
    }

    /**
     * @param baseValues 기준 연도의 월별 방문 값 (키: 1부터 12까지, 없는 달은 키가 없음)
     * 1월 대상은 직전 달이 전년도 12월이라 같은 자료에 없으므로 판단하지 않음
     */
    public static List<Decision> decide(int threshold, int baseYear, Map<Integer, Integer> baseValues,
            List<YearMonth> targets) {
        List<Decision> result = new ArrayList<>();
        for (YearMonth target : targets) {
            int month = target.getMonthValue();
            if (month == 1) {
                continue;
            }
            Integer previous = baseValues.get(month - 1);
            Integer current = baseValues.get(month);
            if (previous == null || current == null || previous <= 0) {
                continue;
            }
            BigDecimal decline = BigDecimal.valueOf(previous - current).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(previous), 1, RoundingMode.HALF_UP);
            if (decline.compareTo(BigDecimal.valueOf(threshold)) >= 0) {
                result.add(new Decision(target, baseYear, month - 1, previous, current, decline));
            }
        }
        return result;
    }

    /** REASON(200바이트)에 들어가는 짧은 판단 이유 */
    public static String reason(Decision d, int threshold) {
        return d.baseYear() + "년 " + d.previousMonth() + "월 대비 " + d.target().getMonthValue() + "월 방문 "
                + d.declineRate().stripTrailingZeros().toPlainString() + "% 감소 · 기준 " + threshold + "% 이상";
    }
}
