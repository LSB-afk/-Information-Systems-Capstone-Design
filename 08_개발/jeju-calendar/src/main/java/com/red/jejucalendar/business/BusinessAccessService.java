package com.red.jejucalendar.business;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.dao.BusinessDao;
import com.red.jejucalendar.dao.BusinessPermissionDao;
import com.red.jejucalendar.domain.Business;

/**
 * 계획·기록 접근 권한 (화면설계서 SCR-C-000, SCR-B-001, SCR-B-002)
 * 사업체 운영자: 본인이 등록한 사업체 또는 BUSINESS_PERMISSION으로 권한을 받은 사업체
 * 서비스 운영자: BUSINESS_PERMISSION으로 관리 권한을 받은 사업체만
 */
@Service
public class BusinessAccessService {

    private final BusinessDao businessDao;
    private final BusinessPermissionDao permissionDao;

    public BusinessAccessService(BusinessDao businessDao, BusinessPermissionDao permissionDao) {
        this.businessDao = businessDao;
        this.permissionDao = permissionDao;
    }

    /** 사업체를 찾고 권한을 확인함 권한이 없으면 403 */
    public Business requireAccess(LoginUser user, long businessId) {
        Business business = businessDao.findById(businessId)
                .orElseThrow(() -> ApiException.notFound("사업체를 찾을 수 없습니다."));
        if (!canAccess(user, business)) {
            throw ApiException.forbidden("이 사업체의 계획·기록을 처리할 권한이 없습니다.");
        }
        return business;
    }

    public boolean canAccess(LoginUser user, Business business) {
        boolean owner = !user.isServiceOperator() && business.userId() == user.userId();
        return owner || permissionDao.find(user.userId(), business.businessId()).isPresent();
    }

    public List<Business> accessibleBusinesses(LoginUser user) {
        Map<Long, Business> result = new LinkedHashMap<>();
        if (!user.isServiceOperator()) {
            businessDao.findByUserId(user.userId()).forEach(b -> result.put(b.businessId(), b));
        }
        businessDao.findAccessibleByUserId(user.userId()).forEach(b -> result.putIfAbsent(b.businessId(), b));
        return new ArrayList<>(result.values());
    }
}
