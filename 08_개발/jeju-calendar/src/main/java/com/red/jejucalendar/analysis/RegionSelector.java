package com.red.jejucalendar.analysis;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.Region;

/** 분석·제안 화면의 지역 선택: 고르지 않았으면 사용자의 첫 사업체 지역, 없으면 첫 번째 지역 */
@Component
public class RegionSelector {

    private final RegionDao regionDao;
    private final BusinessAccessService accessService;

    public RegionSelector(RegionDao regionDao, BusinessAccessService accessService) {
        this.regionDao = regionDao;
        this.accessService = accessService;
    }

    public Region select(LoginUser user, String regionCode) {
        if (regionCode != null && !regionCode.isBlank()) {
            return regionDao.findByCode(regionCode)
                    .orElseThrow(() -> ApiException.badRequest("regionCode", "등록되지 않은 지역입니다."));
        }
        Optional<String> businessRegion = accessService.accessibleBusinesses(user).stream()
                .map(Business::regionCode).findFirst();
        if (businessRegion.isPresent()) {
            return regionDao.findByCode(businessRegion.get()).orElseThrow();
        }
        return regionDao.findAll().stream().findFirst()
                .orElseThrow(() -> ApiException.notFound("등록된 지역이 없습니다. 자료·기준 관리에서 지역을 먼저 확인해 주세요."));
    }
}
