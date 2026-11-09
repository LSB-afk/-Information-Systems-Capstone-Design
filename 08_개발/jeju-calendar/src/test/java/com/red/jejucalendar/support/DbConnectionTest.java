package com.red.jejucalendar.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/** Oracle 접속 확인 (조회만 함) */
@SpringBootTest
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class DbConnectionTest {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void connectsAndListsOwnObjects() {
        String user = jdbc.queryForObject("SELECT USER FROM DUAL", String.class);
        List<String> version = jdbc.queryForList(
                "SELECT BANNER FROM V$VERSION WHERE BANNER LIKE 'Oracle%'", String.class);
        List<String> tables = jdbc.queryForList(
                "SELECT TABLE_NAME FROM USER_TABLES ORDER BY TABLE_NAME", String.class);
        List<String> sequences = jdbc.queryForList(
                "SELECT SEQUENCE_NAME FROM USER_SEQUENCES ORDER BY SEQUENCE_NAME", String.class);

        System.out.println("[DB] 계정: " + user);
        System.out.println("[DB] 버전: " + version);
        System.out.println("[DB] 테이블 " + tables.size() + "개: " + tables);
        System.out.println("[DB] 시퀀스 " + sequences.size() + "개: " + sequences);

        assertThat(user).isNotBlank();
    }
}
