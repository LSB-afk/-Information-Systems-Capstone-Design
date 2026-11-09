package com.red.jejucalendar.plan;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.plan.PlanDtos.BulkAddResult;
import com.red.jejucalendar.plan.PlanDtos.CalendarResponse;
import com.red.jejucalendar.plan.PlanDtos.PlanItem;
import com.red.jejucalendar.plan.PlanDtos.PlanRequest;

/** SCR-B-001 3개월 홍보 일정표, SCR-B-001-P01 계획 작성·수정 */
@RestController
@RequestMapping("/api")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/businesses/{businessId}/calendar")
    public CalendarResponse calendar(@CurrentUser LoginUser user, @PathVariable long businessId) {
        return planService.calendar(user, businessId);
    }

    @PostMapping("/businesses/{businessId}/plans")
    public ResponseEntity<PlanItem> create(@CurrentUser LoginUser user, @PathVariable long businessId,
            @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.create(user, businessId, request));
    }

    @PostMapping("/businesses/{businessId}/plans/from-suggestions")
    public BulkAddResult addAllSuggestions(@CurrentUser LoginUser user, @PathVariable long businessId) {
        return planService.addAllSuggestions(user, businessId);
    }

    @GetMapping("/plans/{planId}")
    public PlanItem get(@CurrentUser LoginUser user, @PathVariable long planId) {
        return planService.get(user, planId);
    }

    @PutMapping("/plans/{planId}")
    public PlanItem update(@CurrentUser LoginUser user, @PathVariable long planId, @RequestBody PlanRequest request) {
        return planService.update(user, planId, request);
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> delete(@CurrentUser LoginUser user, @PathVariable long planId) {
        planService.delete(user, planId);
        return ResponseEntity.noContent().build();
    }
}
