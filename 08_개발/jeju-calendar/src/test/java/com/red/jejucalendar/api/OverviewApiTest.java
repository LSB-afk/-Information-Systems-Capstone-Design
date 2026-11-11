package com.red.jejucalendar.api;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.dao.BusinessDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.Business;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.User;

/**
 * 한눈에 보기·지역 목록 API 확인 (SCR-C-002)
 * 실제 팀 DB를 쓰지만 Transactional 이라 테스트마다 모두 되돌림
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// 기존 실제 자료는 이 테스트 트랜잭션에서만 제외하고 종료 시 원래 상태로 되돌림
@Sql(statements = "UPDATE DATA_SOURCE SET VALIDATION_STATUS='PENDING'")
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class OverviewApiTest {

    static final String PASSWORD = "Test-pass-26!";
    static final String A = "TEST0001";
    static final int[] VISITS = {35, 32, 44, 49, 55, 47, 65, 72, 57, 51, 37, 34};

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired BusinessDao businessDao;

    MockHttpSession owner;
    MockHttpSession operator;
    long businessId;

    @BeforeEach
    void fixtures() throws Exception {
        String hash = encoder.encode(PASSWORD);
        userDao.insert(new User(null, "ov-op@red.example", hash, "SERVICE", null));
        long ownerId = userDao.insert(new User(null, "ov-owner@red.example", hash, "BUSINESS", null));
        regionDao.insert(new Region(A, "테스트읍"));
        businessId = businessDao.insert(new Business(null, ownerId, "테스트 카페", "CAFE", A));
        owner = login("BUSINESS", "ov-owner@red.example");
        operator = login("SERVICE", "ov-op@red.example");

        mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content("{\"declineRateThreshold\":20,\"sourceInfo\":\"지역별 방문객\",\"basePeriod\":\"2025년\","
                        + "\"reasonTemplate\":\"직전 월 대비 방문 감소\",\"requiredCondition\":\"판매 가능 여부 확인\"}"))
                .andExpect(status().isOk());
        StringBuilder csv = new StringBuilder("region_code,month,value,unit,provider,aggregation\n");
        for (int m = 1; m <= 12; m++) {
            csv.append(A).append(",2025-%02d,".formatted(m)).append(VISITS[m - 1]).append(",만 명,테스트 제공자,월별 지역 집계\n");
        }
        mvc.perform(multipart("/api/admin/data-sources").session(operator)
                        .file(new MockMultipartFile("file", "visit.csv", "text/csv", csv.toString().getBytes(StandardCharsets.UTF_8)))
                        .param("sourceVersion", "v1").param("startMonth", "2025-01").param("endMonth", "2025-12")
                        .param("unit", "PERSON_10K").param("providerBasis", "테스트 제공자"))
                .andExpect(status().isCreated());
    }

    @Test
    void ownerSeesTrendSuggestionsCalendarAndDataStatus() throws Exception {
        mvc.perform(get("/api/overview").session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(businessId))
                .andExpect(jsonPath("$.businessName").value("테스트 카페"))
                .andExpect(jsonPath("$.trend.regionCode").value(A))
                .andExpect(jsonPath("$.trend.year").value(2025))
                .andExpect(jsonPath("$.trend.hasData").value(true))
                .andExpect(jsonPath("$.suggestions.totalCount").value(1))
                .andExpect(jsonPath("$.suggestions.canAddToPlan").value(true))
                .andExpect(jsonPath("$.calendar.months", hasSize(3)))
                .andExpect(jsonPath("$.calendar.planCount").value(0))
                .andExpect(jsonPath("$.dataStatus.items", hasSize(4)))
                .andExpect(jsonPath("$.dataStatus.items[0].key").value("VISIT"))
                .andExpect(jsonPath("$.dataStatus.items[0].ready").value(true))
                .andExpect(jsonPath("$.dataStatus.items[3].key").value("CRITERIA"))
                .andExpect(jsonPath("$.dataStatus.items[3].ready").value(true))
                .andExpect(jsonPath("$.dataStatus.readyPercent").value(greaterThanOrEqualTo(50)))
                .andExpect(jsonPath("$.dataStatus.sourceCount").value(greaterThanOrEqualTo(1)));

        // 제안 한 번에 담기 후 계획 요약이 갱신됨 (SCR-C-002 ⑥⑧)
        mvc.perform(post("/api/businesses/" + businessId + "/plans/from-suggestions").session(owner))
                .andExpect(jsonPath("$.added").value(1));
        mvc.perform(get("/api/overview").session(owner))
                .andExpect(jsonPath("$.calendar.planCount").value(1))
                .andExpect(jsonPath("$.calendar.months[1].plans", hasSize(1)));
    }

    @Test
    void operatorWithoutPermittedBusinessHasNoCalendar() throws Exception {
        mvc.perform(get("/api/overview").session(operator).param("regionCode", A).param("metric", "SPEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessId").value(nullValue()))
                .andExpect(jsonPath("$.calendar").value(nullValue()))
                .andExpect(jsonPath("$.trend.metric").value("SPEND"))
                .andExpect(jsonPath("$.suggestions.canAddToPlan").value(false));
        mvc.perform(get("/api/regions").session(operator))
                .andExpect(jsonPath("$[*].regionCode", hasItem(A)));
        mvc.perform(get("/api/overview")).andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String role, String email) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(role, email, PASSWORD)))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
