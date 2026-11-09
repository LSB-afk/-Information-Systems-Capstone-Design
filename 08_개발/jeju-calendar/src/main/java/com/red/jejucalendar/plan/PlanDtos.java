package com.red.jejucalendar.plan;

import java.time.LocalDate;
import java.util.List;

import com.red.jejucalendar.domain.PromoPlan;

/** 계획 화면(SCR-B-001, SCR-B-001-P01)에서 주고받는 데이터 */
public final class PlanDtos {

    private PlanDtos() {
    }

    /** 계획 작성·수정 입력 (제안에서 담을 때만 suggestionId를 보냄, 수정 시에는 무시함) */
    public record PlanRequest(
            String title,
            LocalDate scheduledDate,
            String channel,
            String preparationNote,
            Long suggestionId) {
    }

    public record PlanItem(
            long planId,
            long businessId,
            Long suggestionId,
            String title,
            LocalDate scheduledDate,
            String channel,
            String preparationNote,
            boolean hasRecord) {

        public static PlanItem of(PromoPlan p, boolean hasRecord) {
            return new PlanItem(p.planId(), p.businessId(), p.suggestionId(), p.title(), p.scheduledDate(),
                    p.channel(), p.preparationNote(), hasRecord);
        }
    }

    /** 월별 계획 카드 (계획이 없으면 plans가 비어 있고 화면은 안내 문구와 추가 버튼을 보여줌) */
    public record MonthPlans(String month, List<PlanItem> plans) {
    }

    public record CalendarResponse(
            long businessId,
            String businessName,
            String regionCode,
            LocalDate periodStart,
            LocalDate periodEnd,
            String periodLabel,
            int planCount,
            List<MonthPlans> months) {
    }

    /** 홍보 제안 자동 채우기 결과 */
    public record BulkAddResult(int added, String message, List<PlanItem> plans) {
    }
}
