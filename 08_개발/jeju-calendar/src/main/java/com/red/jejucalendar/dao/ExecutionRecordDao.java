package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.ExecutionRecord;

/** EXECUTION_RECORD 테이블 DAO (PK는 SEQ_EXECUTION_RECORD 시퀀스로 발급, 계획 하나에 기록 하나) */
@Repository
public class ExecutionRecordDao {

    private static final String COLUMNS = "RECORD_ID, PLAN_ID, EXEC_STATUS, EXECUTED_DATE, CHECKED_CLICK_COUNT,"
            + " CHECKED_BOOKING_COUNT, COUPON_USAGE_COUNT, RESULT_MEMO, COLLECTION_METHOD, ADOPTION_STATUS,"
            + " ACTUAL_ACTIVITY, SPENT_TIME_MIN";
    private static final RowMapper<ExecutionRecord> ROW_MAPPER = ExecutionRecordDao::mapRow;

    private final JdbcTemplate jdbc;

    public ExecutionRecordDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 실행 결과 기록을 저장하고 발급된 RECORD_ID를 돌려줌 */
    public long insert(ExecutionRecord r) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_EXECUTION_RECORD");
        jdbc.update("INSERT INTO EXECUTION_RECORD (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, r.planId(), r.execStatus(), JdbcSupport.date(r.executedDate()), r.checkedClickCount(),
                r.checkedBookingCount(), r.couponUsageCount(), r.resultMemo(), r.collectionMethod(),
                r.adoptionStatus(), r.actualActivity(), r.spentTimeMin());
        return id;
    }

    public Optional<ExecutionRecord> findById(long recordId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM EXECUTION_RECORD WHERE RECORD_ID = ?", ROW_MAPPER, recordId)
                .stream().findFirst();
    }

    public Optional<ExecutionRecord> findByPlanId(long planId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM EXECUTION_RECORD WHERE PLAN_ID = ?", ROW_MAPPER, planId)
                .stream().findFirst();
    }

    /** 결과 다시 보기 화면: 사업체의 모든 계획에 대한 기록 */
    public List<ExecutionRecord> findByBusinessId(long businessId) {
        return jdbc.query("SELECT R.RECORD_ID, R.PLAN_ID, R.EXEC_STATUS, R.EXECUTED_DATE, R.CHECKED_CLICK_COUNT,"
                        + " R.CHECKED_BOOKING_COUNT, R.COUPON_USAGE_COUNT, R.RESULT_MEMO, R.COLLECTION_METHOD,"
                        + " R.ADOPTION_STATUS, R.ACTUAL_ACTIVITY, R.SPENT_TIME_MIN"
                        + " FROM EXECUTION_RECORD R JOIN PROMO_PLAN P ON P.PLAN_ID = R.PLAN_ID"
                        + " WHERE P.BUSINESS_ID = ? ORDER BY P.SCHEDULED_DATE, R.RECORD_ID",
                ROW_MAPPER, businessId);
    }

    public int update(ExecutionRecord r) {
        return jdbc.update("UPDATE EXECUTION_RECORD SET EXEC_STATUS = ?, EXECUTED_DATE = ?, CHECKED_CLICK_COUNT = ?,"
                        + " CHECKED_BOOKING_COUNT = ?, COUPON_USAGE_COUNT = ?, RESULT_MEMO = ?, COLLECTION_METHOD = ?,"
                        + " ADOPTION_STATUS = ?, ACTUAL_ACTIVITY = ?, SPENT_TIME_MIN = ? WHERE RECORD_ID = ?",
                r.execStatus(), JdbcSupport.date(r.executedDate()), r.checkedClickCount(), r.checkedBookingCount(),
                r.couponUsageCount(), r.resultMemo(), r.collectionMethod(), r.adoptionStatus(), r.actualActivity(),
                r.spentTimeMin(), r.recordId());
    }

    public int deleteById(long recordId) {
        return jdbc.update("DELETE FROM EXECUTION_RECORD WHERE RECORD_ID = ?", recordId);
    }

    private static ExecutionRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ExecutionRecord(
                rs.getLong("RECORD_ID"),
                rs.getLong("PLAN_ID"),
                rs.getString("EXEC_STATUS"),
                JdbcSupport.localDate(rs, "EXECUTED_DATE"),
                JdbcSupport.integer(rs, "CHECKED_CLICK_COUNT"),
                JdbcSupport.integer(rs, "CHECKED_BOOKING_COUNT"),
                JdbcSupport.integer(rs, "COUPON_USAGE_COUNT"),
                rs.getString("RESULT_MEMO"),
                rs.getString("COLLECTION_METHOD"),
                rs.getString("ADOPTION_STATUS"),
                rs.getString("ACTUAL_ACTIVITY"),
                JdbcSupport.integer(rs, "SPENT_TIME_MIN"));
    }
}
