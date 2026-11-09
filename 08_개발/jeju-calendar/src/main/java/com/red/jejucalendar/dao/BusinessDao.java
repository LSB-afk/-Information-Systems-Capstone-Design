package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.Business;

/** BUSINESS 테이블 DAO (PK는 SEQ_BUSINESS 시퀀스로 발급) */
@Repository
public class BusinessDao {

    private static final String COLUMNS = "BUSINESS_ID, USER_ID, BUSINESS_NAME, CATEGORY, REGION_CODE";
    private static final RowMapper<Business> ROW_MAPPER = BusinessDao::mapRow;

    private final JdbcTemplate jdbc;

    public BusinessDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 사업체를 저장하고 발급된 BUSINESS_ID를 돌려줌 */
    public long insert(Business business) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_BUSINESS");
        jdbc.update("INSERT INTO BUSINESS (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?)",
                id, business.userId(), business.businessName(), business.category(), business.regionCode());
        return id;
    }

    public Optional<Business> findById(long businessId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS WHERE BUSINESS_ID = ?", ROW_MAPPER, businessId)
                .stream().findFirst();
    }

    /** 사업체를 등록한 사용자 기준 목록 */
    public List<Business> findByUserId(long userId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS WHERE USER_ID = ? ORDER BY BUSINESS_ID",
                ROW_MAPPER, userId);
    }

    /** BUSINESS_PERMISSION에 권한(OWNER, MANAGER)이 있는 사업체 목록 */
    public List<Business> findAccessibleByUserId(long userId) {
        return jdbc.query("SELECT B.BUSINESS_ID, B.USER_ID, B.BUSINESS_NAME, B.CATEGORY, B.REGION_CODE"
                + " FROM BUSINESS B JOIN BUSINESS_PERMISSION P ON P.BUSINESS_ID = B.BUSINESS_ID"
                + " WHERE P.USER_ID = ? ORDER BY B.BUSINESS_ID", ROW_MAPPER, userId);
    }

    public List<Business> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS ORDER BY BUSINESS_ID", ROW_MAPPER);
    }

    public int update(Business business) {
        return jdbc.update(
                "UPDATE BUSINESS SET BUSINESS_NAME = ?, CATEGORY = ?, REGION_CODE = ? WHERE BUSINESS_ID = ?",
                business.businessName(), business.category(), business.regionCode(), business.businessId());
    }

    public int deleteById(long businessId) {
        return jdbc.update("DELETE FROM BUSINESS WHERE BUSINESS_ID = ?", businessId);
    }

    private static Business mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Business(
                rs.getLong("BUSINESS_ID"),
                rs.getLong("USER_ID"),
                rs.getString("BUSINESS_NAME"),
                rs.getString("CATEGORY"),
                rs.getString("REGION_CODE"));
    }
}
