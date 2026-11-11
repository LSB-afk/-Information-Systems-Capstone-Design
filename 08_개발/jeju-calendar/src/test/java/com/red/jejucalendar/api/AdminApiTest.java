package com.red.jejucalendar.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.dao.DataValidationResultDao;
import com.red.jejucalendar.dao.RegionDao;
import com.red.jejucalendar.dao.RegionMonthlyStatDao;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.Region;
import com.red.jejucalendar.domain.RegionMonthlyStat;
import com.red.jejucalendar.domain.User;

/**
 * 자료·기준 관리 API 확인 (SCR-A-001, P01, P02)
 * 실제 팀 DB를 쓰지만 Transactional 이라 테스트마다 모두 되돌림
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class AdminApiTest {

    static final String PASSWORD = "Test-pass-26!";
    static final String REGION = "TEST0001";
    static final String GOOD_CSV = "region_code,month,value,unit,provider,aggregation\n"
            + REGION + ",2025-10,51,만 명,테스트 제공자,월별 지역 집계\n"
            + REGION + ",2025-11,37,만 명,테스트 제공자,월별 지역 집계\n";

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired UserDao userDao;
    @Autowired RegionDao regionDao;
    @Autowired RegionMonthlyStatDao monthlyDao;
    @Autowired DataValidationResultDao validationDao;

    MockHttpSession operator;
    MockHttpSession owner;

    @BeforeEach
    void fixtures() throws Exception {
        String hash = encoder.encode(PASSWORD);
        userDao.insert(new User(null, "admin-op@red.example", hash, "SERVICE", null));
        userDao.insert(new User(null, "admin-owner@red.example", hash, "BUSINESS", null));
        regionDao.insert(new Region(REGION, "테스트읍"));
        operator = session("SERVICE", "admin-op@red.example");
        owner = session("BUSINESS", "admin-owner@red.example");
    }

    @Test
    void onlyServiceOperatorsCanUseAdminApi() throws Exception {
        mvc.perform(get("/api/admin/data-sources").session(owner)).andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("자료·기준 관리는 서비스 운영자만 사용할 수 있습니다."));
        upload("/api/admin/data-sources/validate", owner, csv("visit.csv", GOOD_CSV), "2025-01", "2025-12", "PERSON_10K")
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/criteria").session(owner).contentType(MediaType.APPLICATION_JSON)
                .content(criteriaJson(20))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/data-sources")).andExpect(status().isUnauthorized());
    }

    @Test
    void templateDownloadsAsUtf8CsvWithHeader() throws Exception {
        var body = mvc.perform(get("/api/admin/data-sources/template").session(operator))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(body, StandardCharsets.UTF_8))
                .startsWith("﻿region_code,month,value,unit,provider,aggregation");
    }

    @Test
    void validateShowsFiveResultsWithoutSaving() throws Exception {
        int before = count();
        upload("/api/admin/data-sources/validate", operator, csv("visit.csv", GOOD_CSV), "2025-01", "2025-12", "PERSON_10K")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrable").value(true))
                .andExpect(jsonPath("$.rowCount").value(2))
                .andExpect(jsonPath("$.checks", hasSize(5)))
                .andExpect(jsonPath("$.message").value("visit.csv · 2행 · 다섯 검증을 통과했습니다. 자료를 등록할 수 있습니다."));
        // 기준기간을 바꾸면 같은 파일도 다시 판정함
        upload("/api/admin/data-sources/validate", operator, csv("visit.csv", GOOD_CSV), "2025-01", "2025-06", "PERSON_10K")
                .andExpect(jsonPath("$.registrable").value(false))
                .andExpect(jsonPath("$.checks[0].item").value("PERIOD"))
                .andExpect(jsonPath("$.checks[0].passed").value(false));
        assertThat(count()).isEqualTo(before);
    }

    @Test
    void registerRevalidatesThenSavesSourceResultsAndStats() throws Exception {
        int before = count();
        // 검증 실패 자료는 등록 요청을 보내도 저장하지 않음
        upload("/api/admin/data-sources", operator, csv("visit.csv", GOOD_CSV), "2025-01", "2025-12", "KRW_100M")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("등록할 수 없습니다")));
        assertThat(count()).isEqualTo(before);

        String body = upload("/api/admin/data-sources", operator, csv("visit.csv", GOOD_CSV), "2025-01", "2025-12", "PERSON_10K")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.savedRows").value(2))
                .andExpect(jsonPath("$.message").value("자료를 등록했습니다. (2행)"))
                .andReturn().getResponse().getContentAsString();
        Matcher m = Pattern.compile("\"sourceId\":(\\d+)").matcher(body);
        assertThat(m.find()).isTrue();
        long sourceId = Long.parseLong(m.group(1));

        mvc.perform(get("/api/admin/data-sources").session(operator))
                .andExpect(jsonPath("$.count").value(before + 1))
                .andExpect(jsonPath("$.sources[0].sourceId").value(sourceId))
                .andExpect(jsonPath("$.sources[0].validationStatus").value("PASS"))
                .andExpect(jsonPath("$.sources[0].startMonth").value("2025-01-01"));
        assertThat(validationDao.findBySourceId(sourceId)).hasSize(5)
                .allSatisfy(v -> assertThat(v.result()).isEqualTo("PASS"));
        assertThat(monthlyDao.findBySourceId(sourceId))
                .extracting(RegionMonthlyStat::metricType, RegionMonthlyStat::statValue)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("VISIT", java.math.BigDecimal.valueOf(51)), org.assertj.core.groups.Tuple.tuple("VISIT", java.math.BigDecimal.valueOf(37)));
    }

    @Test
    void fileChecks() throws Exception {
        upload("/api/admin/data-sources/validate", operator, csv("visit.txt", GOOD_CSV), "2025-01", "2025-12", "PERSON_10K")
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("file"))
                .andExpect(jsonPath("$.message").value("CSV 파일(.csv)만 등록할 수 있어요."));
        // 엑셀 기본 저장(CP949)처럼 UTF-8이 아닌 파일
        var cp949 = new MockMultipartFile("file", "visit.csv", "text/csv", GOOD_CSV.getBytes(Charset.forName("MS949")));
        upload("/api/admin/data-sources/validate", operator, cp949, "2025-01", "2025-12", "PERSON_10K")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("UTF-8")));
        mvc.perform(multipart("/api/admin/data-sources/validate").session(operator).param("sourceVersion", "v1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("먼저 CSV 파일을 선택해 주세요."));
    }

    @Test
    void criteriaSaveUpdatesTheCurrentRow() throws Exception {
        mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content(criteriaJson(101))).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.field").value("declineRateThreshold"));
        mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content("{\"declineRateThreshold\":20,\"sourceInfo\":\"\",\"basePeriod\":\"2025년\","
                        + "\"reasonTemplate\":\"이유\",\"requiredCondition\":\"조건\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.field").value("sourceInfo"));

        String first = mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content(criteriaJson(20))).andExpect(status().isOk())
                .andExpect(jsonPath("$.declineRateThreshold").value(20))
                .andReturn().getResponse().getContentAsString();
        String id = first.replaceAll(".*\"criteriaId\":(\\d+).*", "$1");
        mvc.perform(put("/api/admin/criteria").session(operator).contentType(MediaType.APPLICATION_JSON)
                .content(criteriaJson(25))).andExpect(jsonPath("$.criteriaId").value(Long.parseLong(id)));
        mvc.perform(get("/api/admin/criteria").session(operator))
                .andExpect(jsonPath("$.declineRateThreshold").value(25))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    // ---------- 도우미 ----------

    private int count() throws Exception {
        String body = mvc.perform(get("/api/admin/data-sources").session(operator))
                .andReturn().getResponse().getContentAsString();
        return Integer.parseInt(body.replaceAll("^\\{\"count\":(\\d+).*", "$1"));
    }

    private static MockMultipartFile csv(String name, String text) {
        return new MockMultipartFile("file", name, "text/csv", text.getBytes(StandardCharsets.UTF_8));
    }

    private ResultActions upload(String url, MockHttpSession s, MockMultipartFile file, String from, String to,
            String unit) throws Exception {
        return mvc.perform(multipart(url).file(file).session(s)
                .param("sourceVersion", "테스트 방문 자료 v1")
                .param("startMonth", from)
                .param("endMonth", to)
                .param("unit", unit)
                .param("providerBasis", "테스트 제공자"));
    }

    private static String criteriaJson(int threshold) {
        return ("{\"declineRateThreshold\":%d,\"sourceInfo\":\"지역별 방문객 2025\",\"basePeriod\":\"2025년\","
                + "\"reasonTemplate\":\"직전 월 대비 방문 감소\",\"requiredCondition\":\"판매 가능 여부 확인\"}")
                .formatted(threshold);
    }

    private MockHttpSession session(String role, String email) throws Exception {
        return (MockHttpSession) mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(role, email, PASSWORD)))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
