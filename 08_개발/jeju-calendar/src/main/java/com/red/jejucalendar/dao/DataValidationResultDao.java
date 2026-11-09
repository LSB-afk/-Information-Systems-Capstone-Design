package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.DataValidationResult;

/** DATA_VALIDATION_RESULT 테이블 DAO (복합 PK: SOURCE_ID + VALIDATION_ITEM) */
@Repository
public class DataValidationResultDao {

    private static final String COLUMNS = "SOURCE_ID, VALIDATION_ITEM, RESULT, REASON, VALIDATED_AT";
    private static final RowMapper<DataValidationResult> ROW_MAPPER = DataValidationResultDao::mapRow;

    private final JdbcTemplate jdbc;

    public DataValidationResultDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(DataValidationResult result) {
        jdbc.update("INSERT INTO DATA_VALIDATION_RESULT (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?)",
                result.sourceId(), result.validationItem(), result.result(), result.reason(),
                JdbcSupport.date(result.validatedAt()));
    }

    public Optional<DataValidationResult> find(long sourceId, String validationItem) {
        return jdbc.query("SELECT " + COLUMNS + " FROM DATA_VALIDATION_RESULT WHERE SOURCE_ID = ? AND VALIDATION_ITEM = ?",
                ROW_MAPPER, sourceId, validationItem).stream().findFirst();
    }

    /** 자료 하나의 검증 항목 다섯 개 결과 */
    public List<DataValidationResult> findBySourceId(long sourceId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM DATA_VALIDATION_RESULT WHERE SOURCE_ID = ? ORDER BY VALIDATION_ITEM",
                ROW_MAPPER, sourceId);
    }

    public int update(DataValidationResult result) {
        return jdbc.update("UPDATE DATA_VALIDATION_RESULT SET RESULT = ?, REASON = ?, VALIDATED_AT = ?"
                        + " WHERE SOURCE_ID = ? AND VALIDATION_ITEM = ?",
                result.result(), result.reason(), JdbcSupport.date(result.validatedAt()),
                result.sourceId(), result.validationItem());
    }

    public int delete(long sourceId, String validationItem) {
        return jdbc.update("DELETE FROM DATA_VALIDATION_RESULT WHERE SOURCE_ID = ? AND VALIDATION_ITEM = ?",
                sourceId, validationItem);
    }

    /** 자료를 다시 검증할 때 이전 결과를 지움 */
    public int deleteBySourceId(long sourceId) {
        return jdbc.update("DELETE FROM DATA_VALIDATION_RESULT WHERE SOURCE_ID = ?", sourceId);
    }

    private static DataValidationResult mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DataValidationResult(
                rs.getLong("SOURCE_ID"),
                rs.getString("VALIDATION_ITEM"),
                rs.getString("RESULT"),
                rs.getString("REASON"),
                JdbcSupport.localDate(rs, "VALIDATED_AT"));
    }
}
