package com.red.jejucalendar.record;

import java.time.LocalDate;
import java.util.List;

import com.red.jejucalendar.plan.PlanDtos.PlanItem;

/** 실행 기록 화면(SCR-B-002, SCR-B-002-P01, SCR-B-002-P02)에서 주고받는 데이터 */
public final class RecordDtos {

    private RecordDtos() {
    }

    /**
     * 기록 입력·수정 값
     * 숫자 칸의 null은 미확보·미측정, 0은 실제로 확인한 0건
     */
    public record RecordRequest(
            String execStatus,
            LocalDate executedDate,
            String actualActivity,
            String adoptionStatus,
            Integer spentTimeMin,
            Integer checkedClickCount,
            Integer checkedBookingCount,
            Integer couponUsageCount,
            String resultMemo,
            String collectionMethod) {
    }

    /**
     * 기록 입력 폼 (SCR-B-002-P01 ⑬)
     * 최초 작성이면 recordId가 null이고 prefilled가 true, values는 계획 값으로 채운 기본값
     */
    public record RecordForm(PlanItem plan, Long recordId, boolean prefilled, RecordRequest values) {
    }

    public record RecordView(long recordId, long planId, RecordRequest values) {
    }

    /** 목록 한 줄 (기록이 없으면 record가 null, 미기록은 실행 실패가 아님) */
    public record RecordRow(PlanItem plan, RecordView record) {
    }

    /** SCR-B-002: 요약 카드 + 목록 */
    public record RecordListResponse(
            String periodLabel,
            int planCount,
            int recordedCount,
            int unrecordedCount,
            List<RecordRow> rows) {
    }

    /**
     * 입력한 값만 더한 합계와 그 값을 입력한 기록 수
     * total이 null이면 입력된 값이 하나도 없는 미확보 상태
     */
    public record InputTotal(Integer total, int inputCount) {
    }

    /** SCR-B-002-P02 사용 결과 요약 (매출 증가·인과 효과는 추정하지 않음) */
    public record RecordSummary(
            int planCount,
            int recordCount,
            int doneCount,
            int adoptedYes,
            int adoptedNo,
            int adoptedUnknown,
            InputTotal spentTimeMin,
            InputTotal clicks,
            InputTotal bookings,
            InputTotal coupons,
            List<RecordRow> rows) {
    }
}
