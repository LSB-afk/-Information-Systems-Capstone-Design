package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.DataSourceFile;

/** DATA_SOURCE 테이블 DAO (PK는 SEQ_DATA_SOURCE 시퀀스로 발급) */
@Repository
public class DataSourceFileDao {

    private static final String COLUMNS = "SOURCE_ID, USER_ID, FILE_NAME, SOURCE_VERSION, START_MONTH, END_MONTH,"
            + " UNIT, VALIDATION_STATUS, PROVIDER_BASIS, ROW_COUNT, REGISTERED_AT";
    private static final RowMapper<DataSourceFile> ROW_MAPPER = DataSourceFileDao::mapRow;

    private final JdbcTemplate jdbc;

    public DataSourceFileDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 자료를 저장하고 발급된 SOURCE_ID를 돌려줌 */
    public long insert(DataSourceFile source) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_DATA_SOURCE");
        jdbc.update("INSERT INTO DATA_SOURCE (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, source.userId(), source.fileName(), source.sourceVersion(),
                JdbcSupport.date(source.startMonth()), JdbcSupport.date(source.endMonth()),
                source.unit(), source.validationStatus(), source.providerBasis(), source.rowCount(),
                JdbcSupport.date(source.registeredAt()));
        return id;
    }

    public Optional<DataSourceFile> findById(long sourceId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM DATA_SOURCE WHERE SOURCE_ID = ?", ROW_MAPPER, sourceId)
                .stream().findFirst();
    }

    /** 최근 등록한 자료부터 */
    public List<DataSourceFile> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM DATA_SOURCE ORDER BY REGISTERED_AT DESC, SOURCE_ID DESC",
                ROW_MAPPER);
    }

    /** 검증 상태(PASS, FAIL, PENDING)별 목록 */
    public List<DataSourceFile> findByValidationStatus(String validationStatus) {
        return jdbc.query("SELECT " + COLUMNS + " FROM DATA_SOURCE WHERE VALIDATION_STATUS = ? ORDER BY SOURCE_ID",
                ROW_MAPPER, validationStatus);
    }

    public int update(DataSourceFile source) {
        return jdbc.update("UPDATE DATA_SOURCE SET FILE_NAME = ?, SOURCE_VERSION = ?, START_MONTH = ?, END_MONTH = ?,"
                        + " UNIT = ?, VALIDATION_STATUS = ?, PROVIDER_BASIS = ?, ROW_COUNT = ? WHERE SOURCE_ID = ?",
                source.fileName(), source.sourceVersion(),
                JdbcSupport.date(source.startMonth()), JdbcSupport.date(source.endMonth()),
                source.unit(), source.validationStatus(), source.providerBasis(), source.rowCount(),
                source.sourceId());
    }

    public int updateValidationStatus(long sourceId, String validationStatus) {
        return jdbc.update("UPDATE DATA_SOURCE SET VALIDATION_STATUS = ? WHERE SOURCE_ID = ?",
                validationStatus, sourceId);
    }

    public int deleteById(long sourceId) {
        return jdbc.update("DELETE FROM DATA_SOURCE WHERE SOURCE_ID = ?", sourceId);
    }

    private static DataSourceFile mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new DataSourceFile(
                rs.getLong("SOURCE_ID"),
                rs.getLong("USER_ID"),
                rs.getString("FILE_NAME"),
                rs.getString("SOURCE_VERSION"),
                JdbcSupport.localDate(rs, "START_MONTH"),
                JdbcSupport.localDate(rs, "END_MONTH"),
                rs.getString("UNIT"),
                rs.getString("VALIDATION_STATUS"),
                rs.getString("PROVIDER_BASIS"),
                JdbcSupport.integer(rs, "ROW_COUNT"),
                JdbcSupport.localDate(rs, "REGISTERED_AT"));
    }
}
