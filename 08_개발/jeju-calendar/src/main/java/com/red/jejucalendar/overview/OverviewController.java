package com.red.jejucalendar.overview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.red.jejucalendar.auth.CurrentUser;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.overview.OverviewService.OverviewResponse;

/** SCR-C-002 한눈에 보기 */
@RestController
public class OverviewController {

    private final OverviewService service;

    public OverviewController(OverviewService service) {
        this.service = service;
    }

    /** regionCode·businessId를 비우면 처리할 수 있는 첫 사업체와 그 지역, metric은 VISIT(기본) 또는 SPEND */
    @GetMapping("/api/overview")
    public OverviewResponse overview(@CurrentUser LoginUser user,
            @RequestParam(required = false) String regionCode,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String metric) {
        return service.overview(user, regionCode, businessId, metric);
    }
}
