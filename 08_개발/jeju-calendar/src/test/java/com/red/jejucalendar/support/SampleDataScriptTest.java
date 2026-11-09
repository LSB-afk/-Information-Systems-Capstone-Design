package com.red.jejucalendar.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.auth.AuthService;
import com.red.jejucalendar.auth.LoginUser;
import com.red.jejucalendar.business.BusinessAccessService;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.plan.PlanService;

/**
 * db/03_sample_data.sql이 실제 DB에서 오류 없이 실행되고 시연 계정으로 로그인되는지 확인
 * Transactional 이라 끝나면 되돌려 샘플 데이터가 남지 않음 (이미 넣은 DB에서는 건너뜀)
 */
@SpringBootTest
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class SampleDataScriptTest {

    @Autowired DataSource dataSource;
    @Autowired AuthService authService;
    @Autowired BusinessAccessService accessService;
    @Autowired PlanService planService;
    @Autowired UserDao userDao;

    @Test
    void sampleDataLoadsAndDemoAccountsLogIn() {
        assumeTrue(userDao.findByEmail("cafe@example.com").isEmpty(), "샘플 데이터가 이미 들어 있음");
        var connection = DataSourceUtils.getConnection(dataSource);
        ScriptUtils.executeSqlScript(connection,
                new EncodedResource(new FileSystemResource("db/03_sample_data.sql"), StandardCharsets.UTF_8));

        LoginUser owner = authService.login("BUSINESS", "cafe@example.com", "JejuDemo26!");
        LoginUser operator = authService.login("SERVICE", "operator@example.com", "JejuDemo26!");

        var business = accessService.accessibleBusinesses(owner).get(0);
        assertThat(business.businessName()).isEqualTo("애월 시연 카페");
        assertThat(accessService.accessibleBusinesses(operator)).hasSize(1);
        assertThat(planService.calendar(owner, business.businessId()).planCount()).isEqualTo(2);
    }
}
