package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.BusinessPermission;

/** BUSINESS_PERMISSION 테이블 DAO (복합 PK: USER_ID + BUSINESS_ID) */
@Repository
public class BusinessPermissionDao {

    private static final String COLUMNS = "USER_ID, BUSINESS_ID, PERMISSION_TYPE, GRANTED_AT";
    private static final RowMapper<BusinessPermission> ROW_MAPPER = BusinessPermissionDao::mapRow;

    private final JdbcTemplate jdbc;

    public BusinessPermissionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(BusinessPermission permission) {
        jdbc.update("INSERT INTO BUSINESS_PERMISSION (" + COLUMNS + ") VALUES (?, ?, ?, ?)",
                permission.userId(), permission.businessId(), permission.permissionType(),
                JdbcSupport.date(permission.grantedAt()));
    }

    public Optional<BusinessPermission> find(long userId, long businessId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS_PERMISSION WHERE USER_ID = ? AND BUSINESS_ID = ?",
                ROW_MAPPER, userId, businessId).stream().findFirst();
    }

    public List<BusinessPermission> findByUserId(long userId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS_PERMISSION WHERE USER_ID = ? ORDER BY BUSINESS_ID",
                ROW_MAPPER, userId);
    }

    public List<BusinessPermission> findByBusinessId(long businessId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM BUSINESS_PERMISSION WHERE BUSINESS_ID = ? ORDER BY USER_ID",
                ROW_MAPPER, businessId);
    }

    public int update(BusinessPermission permission) {
        return jdbc.update(
                "UPDATE BUSINESS_PERMISSION SET PERMISSION_TYPE = ?, GRANTED_AT = ? WHERE USER_ID = ? AND BUSINESS_ID = ?",
                permission.permissionType(), JdbcSupport.date(permission.grantedAt()),
                permission.userId(), permission.businessId());
    }

    public int delete(long userId, long businessId) {
        return jdbc.update("DELETE FROM BUSINESS_PERMISSION WHERE USER_ID = ? AND BUSINESS_ID = ?", userId, businessId);
    }

    private static BusinessPermission mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BusinessPermission(
                rs.getLong("USER_ID"),
                rs.getLong("BUSINESS_ID"),
                rs.getString("PERMISSION_TYPE"),
                JdbcSupport.localDate(rs, "GRANTED_AT"));
    }
}
