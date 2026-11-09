package com.red.jejucalendar.record;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.common.Codes;
import com.red.jejucalendar.common.Inputs;
import com.red.jejucalendar.dao.ExecutionRecordDao;
import com.red.jejucalendar.dao.PromoPlanDao;
import com.red.jejucalendar.domain.ExecutionRecord;
import com.red.jejucalendar.domain.PromoPlan;
import com.red.jejucalendar.plan.CalendarPeriod;
import com.red.jejucalendar.plan.PlanDtos.PlanItem;
import com.red.jejucalendar.record.RecordDtos.InputTotal;
import com.red.jejucalendar.record.RecordDtos.RecordForm;
import com.red.jejucalendar.record.RecordDtos.RecordListResponse;
import com.red.jejucalendar.record.RecordDtos.RecordRequest;
import com.red.jejucalendar.record.RecordDtos.RecordRow;
import com.red.jejucalendar.record.RecordDtos.RecordSummary;
import com.red.jejucalendar.record.RecordDtos.RecordView;

/** UC04 사용·실행 기록 관리, UC05 계획 이행·실행 결과 조회 (SCR-B-002, P01, P02) */
@Service
public class RecordService {

    private static final Map<String, String> CHANNEL_LABELS = Map.of(
            "INSTAGRAM", "인스타그램", "STORE", "매장 안내", "BLOG", "블로그", "ETC", "기타");

    private final BusinessAccessService accessService;
    private final PromoPlanDao planDao;
    private final ExecutionRecordDao recordDao;
    private final CalendarPeriod period;

    public RecordService(BusinessAccessService accessService, PromoPlanDao planDao, ExecutionRecordDao recordDao,
            CalendarPeriod period) {
        this.accessService = accessService;
        this.planDao = planDao;
        this.recordDao = recordDao;
        this.period = period;
    }

    /** SCR-B-002 ①③: 일정표 기간의 계획과 기록 */
    public RecordListResponse list(LoginUser user, long businessId) {
        List<RecordRow> rows = rows(user, businessId);
        int recorded = (int) rows.stream().filter(r -> r.record() != null).count();
        return new RecordListResponse(period.label(), rows.size(), recorded, rows.size() - recorded, rows);
    }

    /** SCR-B-002-P01: 최초 작성은 계획 값을 기본값으로, 수정은 저장된 기록을 불러옴 */
    public RecordForm form(LoginUser user, long planId) {
        PromoPlan plan = requirePlan(user, planId);
        PlanItem item = PlanItem.of(plan, false);
        return recordDao.findByPlanId(planId)
                .map(r -> new RecordForm(PlanItem.of(plan, true), r.recordId(), false, toRequest(r)))
                .orElseGet(() -> new RecordForm(item, null, true, defaults(plan)));
    }

    /** SCR-B-002-P01 ⑫: 권한·입력 검사 후 저장 (계획 하나에 기록 하나) */
    @Transactional
    public RecordView save(LoginUser user, long planId, RecordRequest request) {
        PromoPlan plan = requirePlan(user, planId);
        ExecutionRecord existing = recordDao.findByPlanId(planId).orElse(null);
        ExecutionRecord record = validated(existing == null ? null : existing.recordId(), plan.planId(), request);
        long recordId;
        if (existing == null) {
            recordId = recordDao.insert(record);
        } else {
            recordDao.update(record);
            recordId = existing.recordId();
        }
        return toView(recordDao.findById(recordId).orElseThrow());
    }

    /** SCR-B-002-P02: 권한 내 기록 집계 (미확보 값은 0으로 바꾸지 않음) */
    public RecordSummary summary(LoginUser user, long businessId) {
        List<RecordRow> rows = rows(user, businessId);
        List<RecordRequest> records = rows.stream()
                .map(RecordRow::record).filter(Objects::nonNull).map(RecordView::values).toList();
        return new RecordSummary(
                rows.size(),
                records.size(),
                count(records, r -> Codes.EXEC_DONE.equals(r.execStatus())),
                count(records, r -> "YES".equals(r.adoptionStatus())),
                count(records, r -> "NO".equals(r.adoptionStatus())),
                count(records, r -> Codes.ADOPTION_UNKNOWN.equals(r.adoptionStatus())),
                total(records, RecordRequest::spentTimeMin),
                total(records, RecordRequest::checkedClickCount),
                total(records, RecordRequest::checkedBookingCount),
                total(records, RecordRequest::couponUsageCount),
                rows);
    }

    private List<RecordRow> rows(LoginUser user, long businessId) {
        accessService.requireAccess(user, businessId);
        Map<Long, ExecutionRecord> records = recordDao.findByBusinessId(businessId).stream()
                .collect(Collectors.toMap(ExecutionRecord::planId, Function.identity()));
        return planDao.findByBusinessIdAndPeriod(businessId, period.start(), period.end()).stream()
                .map(p -> {
                    ExecutionRecord r = records.get(p.planId());
                    return new RecordRow(PlanItem.of(p, r != null), r == null ? null : toView(r));
                })
                .toList();
    }

    /** 자동 채움: 실행함, 계획 날짜, 채널·준비 내용, 제안에서 담은 계획이면 채택 */
    private RecordRequest defaults(PromoPlan plan) {
        String channel = CHANNEL_LABELS.getOrDefault(plan.channel(), plan.channel());
        String activity = plan.preparationNote() == null ? channel : channel + " · " + plan.preparationNote();
        String adoption = plan.suggestionId() != null ? Codes.ADOPTION_YES : Codes.ADOPTION_UNKNOWN;
        return new RecordRequest(Codes.EXEC_DONE, plan.scheduledDate(), activity, adoption,
                null, null, null, null, null, null);
    }

    private ExecutionRecord validated(Long recordId, long planId, RecordRequest r) {
        String status = Inputs.code("execStatus", r.execStatus(), Codes.EXEC_STATUSES, "실행 여부를 선택해 주세요.");
        if (Codes.EXEC_DONE.equals(status) && r.executedDate() == null) {
            throw ApiException.badRequest("executedDate", "실행함을 선택했으면 실제 실행 날짜를 입력해 주세요.");
        }
        String adoption = Inputs.code("adoptionStatus", r.adoptionStatus(), Codes.ADOPTION_STATUSES,
                "추천 채택 여부를 선택해 주세요.");
        String activity = Inputs.maxBytes("actualActivity", "실행한 활동", Inputs.trimToNull(r.actualActivity()), 500);
        String memo = Inputs.maxBytes("resultMemo", "결과", Inputs.trimToNull(r.resultMemo()), 500);
        String method = Inputs.maxBytes("collectionMethod", "수집 방법", Inputs.trimToNull(r.collectionMethod()), 200);
        return new ExecutionRecord(recordId, planId, status, r.executedDate(),
                Inputs.count("checkedClickCount", "클릭 수", r.checkedClickCount()),
                Inputs.count("checkedBookingCount", "예약 수", r.checkedBookingCount()),
                Inputs.count("couponUsageCount", "쿠폰 사용 수", r.couponUsageCount()),
                memo, method, adoption, activity,
                Inputs.count("spentTimeMin", "계획 소요 시간", r.spentTimeMin()));
    }

    private PromoPlan requirePlan(LoginUser user, long planId) {
        PromoPlan plan = planDao.findById(planId)
                .orElseThrow(() -> ApiException.notFound("계획을 찾을 수 없습니다. 목록을 새로 고쳐 주세요."));
        accessService.requireAccess(user, plan.businessId());
        return plan;
    }

    private static RecordRequest toRequest(ExecutionRecord r) {
        return new RecordRequest(r.execStatus(), r.executedDate(), r.actualActivity(), r.adoptionStatus(),
                r.spentTimeMin(), r.checkedClickCount(), r.checkedBookingCount(), r.couponUsageCount(),
                r.resultMemo(), r.collectionMethod());
    }

    private static RecordView toView(ExecutionRecord r) {
        return new RecordView(r.recordId(), r.planId(), toRequest(r));
    }

    private static int count(List<RecordRequest> records, java.util.function.Predicate<RecordRequest> test) {
        return (int) records.stream().filter(test).count();
    }

    private static InputTotal total(List<RecordRequest> records, Function<RecordRequest, Integer> value) {
        List<Integer> inputs = records.stream().map(value).filter(Objects::nonNull).toList();
        return new InputTotal(inputs.isEmpty() ? null : inputs.stream().mapToInt(Integer::intValue).sum(),
                inputs.size());
    }
}
