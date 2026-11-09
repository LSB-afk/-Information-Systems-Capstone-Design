package com.red.jejucalendar.dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;

/** DAO 공통 도우미: 시퀀스 번호 발급, DATE·NULL 변환 */
final class JdbcSupport {

    private JdbcSupport() {
    }

    /** 시퀀스에서 다음 번호를 받음 (예: SEQ_USERS) */
    static long nextVal(JdbcTemplate jdbc, String sequenceName) {
        Long id = jdbc.queryForObject("SELECT " + sequenceName + ".NEXTVAL FROM DUAL", Long.class);
        if (id == null) {
            throw new IllegalStateException(sequenceName + " 번호를 받지 못함");
        }
        return id;
    }

    static Date date(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    static Integer integer(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    static Long longValue(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    /** batchUpdate 결과에서 실패한 행 수 (Oracle은 성공 행을 SUCCESS_NO_INFO로 돌려줌) */
    static int countFailures(int[][] counts) {
        int failures = 0;
        for (int[] batch : counts) {
            for (int c : batch) {
                if (c == java.sql.Statement.EXECUTE_FAILED) {
                    failures++;
                }
            }
        }
        return failures;
    }
}
