package com.red.jejucalendar.record;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.record.RecordDtos.RecordForm;
import com.red.jejucalendar.record.RecordDtos.RecordListResponse;
import com.red.jejucalendar.record.RecordDtos.RecordRequest;
import com.red.jejucalendar.record.RecordDtos.RecordSummary;
import com.red.jejucalendar.record.RecordDtos.RecordView;

/** SCR-B-002 실행 결과 조회, SCR-B-002-P01 기록 입력·수정, SCR-B-002-P02 사용 결과 요약 */
@RestController
@RequestMapping("/api")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @GetMapping("/businesses/{businessId}/records")
    public RecordListResponse list(@CurrentUser LoginUser user, @PathVariable long businessId) {
        return recordService.list(user, businessId);
    }

    @GetMapping("/businesses/{businessId}/records/summary")
    public RecordSummary summary(@CurrentUser LoginUser user, @PathVariable long businessId) {
        return recordService.summary(user, businessId);
    }

    @GetMapping("/plans/{planId}/record")
    public RecordForm form(@CurrentUser LoginUser user, @PathVariable long planId) {
        return recordService.form(user, planId);
    }

    @PutMapping("/plans/{planId}/record")
    public RecordView save(@CurrentUser LoginUser user, @PathVariable long planId, @RequestBody RecordRequest request) {
        return recordService.save(user, planId, request);
    }
}
