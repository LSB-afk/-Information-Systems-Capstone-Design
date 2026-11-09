package com.red.jejucalendar.plan;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import com.red.jejucalendar.dao.PromoSuggestionDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.ExecutionRecord;
import com.red.jejucalendar.domain.PromoPlan;
import com.red.jejucalendar.domain.PromoSuggestion;
import com.red.jejucalendar.plan.PlanDtos.BulkAddResult;
import com.red.jejucalendar.plan.PlanDtos.CalendarResponse;
import com.red.jejucalendar.plan.PlanDtos.MonthPlans;
import com.red.jejucalendar.plan.PlanDtos.PlanItem;
import com.red.jejucalendar.plan.PlanDtos.PlanRequest;

/** UC03 3개월 홍보 일정표 관리 (SCR-B-001, SCR-B-001-P01) */
@Service
public class PlanService {

    private final BusinessAccessService accessService;
    private final PromoPlanDao planDao;
    private final PromoSuggestionDao suggestionDao;
    private final ExecutionRecordDao recordDao;
    private final CalendarPeriod period;

    public PlanService(BusinessAccessService accessService, PromoPlanDao planDao, PromoSuggestionDao suggestionDao,
            ExecutionRecordDao recordDao, CalendarPeriod period) {
        this.accessService = accessService;
        this.planDao = planDao;
        this.suggestionDao = suggestionDao;
        this.recordDao = recordDao;
        this.period = period;
    }

    /** SCR-B-001 ③⑦: 3개월의 월별 계획 카드 */
    public CalendarResponse calendar(LoginUser user, long businessId) {
        Business business = accessService.requireAccess(user, businessId);
        List<PromoPlan> plans = planDao.findByBusinessIdAndPeriod(businessId, period.start(), period.end());
        Set<Long> recorded = recordedPlanIds(businessId);
        List<MonthPlans> months = new ArrayList<>();
        for (YearMonth month : period.months()) {
            List<PlanItem> items = plans.stream()
                    .filter(p -> YearMonth.from(p.scheduledDate()).equals(month))
                    .map(p -> PlanItem.of(p, recorded.contains(p.planId())))
                    .toList();
            months.add(new MonthPlans(month.toString(), items));
        }
        return new CalendarResponse(businessId, business.businessName(), business.regionCode(),
                period.start(), period.end(), period.label(), plans.size(), months);
    }

    /** SCR-B-001-P01 ④: 저장된 값 조회 */
    public PlanItem get(LoginUser user, long planId) {
        PromoPlan plan = requirePlan(user, planId);
        return PlanItem.of(plan, recordDao.findByPlanId(planId).isPresent());
    }

    /** SCR-B-001-P01 ⑧: 직접 추가·월별 추가·개별 제안 담기 */
    public PlanItem create(LoginUser user, long businessId, PlanRequest request) {
        Business business = accessService.requireAccess(user, businessId);
        Long suggestionId = request.suggestionId();
        if (suggestionId != null) {
            PromoSuggestion suggestion = suggestionDao.findById(suggestionId)
                    .orElseThrow(() -> ApiException.notFound("현재 추천 조건에 맞지 않는 제안입니다. 다시 조회해 주세요."));
            if (!suggestion.regionCode().equals(business.regionCode())) {
                throw ApiException.badRequest(null, "사업체 지역의 제안만 담을 수 있습니다.");
            }
            if (alreadyAdded(businessId).contains(suggestionId)) {
                throw ApiException.conflict("이미 담은 제안입니다. 저장된 계획을 수정할 수 있어요.");
            }
        }
        PromoPlan plan = validated(null, businessId, suggestionId, request);
        long planId = planDao.insert(plan);
        return PlanItem.of(planDao.findById(planId).orElseThrow(), false);
    }

    /** SCR-B-001-P01 ⑧: 수정 (사업체와 연결된 제안은 바꾸지 않음) */
    public PlanItem update(LoginUser user, long planId, PlanRequest request) {
        PromoPlan old = requirePlan(user, planId);
        PromoPlan plan = validated(planId, old.businessId(), old.suggestionId(), request);
        planDao.update(plan);
        return PlanItem.of(planDao.findById(planId).orElseThrow(), recordDao.findByPlanId(planId).isPresent());
    }

    /** SCR-B-001-P01 ⑩: 계획과 연결된 실행 기록 삭제 (FK ON DELETE CASCADE) */
    public void delete(LoginUser user, long planId) {
        requirePlan(user, planId);
        planDao.deleteById(planId);
    }

    /**
     * SCR-B-001 ⑧ 홍보 제안 자동 채우기
     * 사업체 지역의 3개월 제안 중 아직 담지 않은 제안만 저장하고 기존 계획은 유지함
     * 하나라도 실패하면 전체를 되돌려 추가 전 상태로 복구함
     */
    @Transactional
    public BulkAddResult addAllSuggestions(LoginUser user, long businessId) {
        Business business = accessService.requireAccess(user, businessId);
        Set<Long> added = alreadyAdded(businessId);
        List<PlanItem> created = new ArrayList<>();
        for (YearMonth month : period.months()) {
            for (PromoSuggestion s : suggestionDao.findByRegionAndMonth(business.regionCode(), month.atDay(1))) {
                if (!added.add(s.suggestionId())) {
                    continue;
                }
                LocalDate date = period.contains(s.recommendedDate()) ? s.recommendedDate() : month.atDay(15);
                String channel = s.channel() != null && Codes.CHANNELS.contains(s.channel())
                        ? s.channel() : Codes.CHANNEL_INSTAGRAM;
                PromoPlan plan = validated(null, businessId, s.suggestionId(),
                        new PlanRequest(s.title(), date, channel, s.preparationTask(), s.suggestionId()));
                long planId = planDao.insert(plan);
                created.add(PlanItem.of(planDao.findById(planId).orElseThrow(), false));
            }
        }
        String message = created.isEmpty()
                ? "이미 모든 제안을 담았거나 담을 제안이 없습니다."
                : "제안 " + created.size() + "개를 일정에 담았습니다.";
        return new BulkAddResult(created.size(), message, created);
    }

    private PromoPlan validated(Long planId, long businessId, Long suggestionId, PlanRequest r) {
        String title = Inputs.required("title", r.title(), "계획 이름을 입력해 주세요.");
        Inputs.maxBytes("title", "계획 이름", title, 100);
        if (r.scheduledDate() == null) {
            throw ApiException.badRequest("scheduledDate", "예정 날짜를 입력해 주세요.");
        }
        if (!period.contains(r.scheduledDate())) {
            throw ApiException.badRequest("scheduledDate", "예정 날짜는 " + period.label() + " 안에서 골라 주세요.");
        }
        String channel = Inputs.code("channel", r.channel(), Codes.CHANNELS, "홍보 채널을 선택해 주세요.");
        String note = Inputs.maxBytes("preparationNote", "준비할 내용", Inputs.trimToNull(r.preparationNote()), 500);
        return new PromoPlan(planId, businessId, suggestionId, title, r.scheduledDate(), channel, note);
    }

    private PromoPlan requirePlan(LoginUser user, long planId) {
        PromoPlan plan = planDao.findById(planId)
                .orElseThrow(() -> ApiException.notFound("계획을 찾을 수 없습니다. 목록을 새로 고쳐 주세요."));
        accessService.requireAccess(user, plan.businessId());
        return plan;
    }

    private Set<Long> alreadyAdded(long businessId) {
        return planDao.findByBusinessId(businessId).stream()
                .map(PromoPlan::suggestionId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    private Set<Long> recordedPlanIds(long businessId) {
        return recordDao.findByBusinessId(businessId).stream()
                .map(ExecutionRecord::planId)
                .collect(Collectors.toSet());
    }
}
