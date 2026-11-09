package com.red.jejucalendar.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.red.jejucalendar.domain.User;

/** USERS 테이블 DAO (PK는 SEQ_USERS 시퀀스로 발급) */
@Repository
public class UserDao {

    private static final String COLUMNS = "USER_ID, EMAIL, PASSWORD_HASH, ROLE, CREATED_AT";
    private static final RowMapper<User> ROW_MAPPER = UserDao::mapRow;

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 새 사용자를 저장하고 발급된 USER_ID를 돌려줌 */
    public long insert(User user) {
        long id = JdbcSupport.nextVal(jdbc, "SEQ_USERS");
        jdbc.update(
                "INSERT INTO USERS (" + COLUMNS + ") VALUES (?, ?, ?, ?, ?)",
                id, user.email(), user.passwordHash(), user.role(),
                JdbcSupport.date(user.createdAt()));
        return id;
    }

    public Optional<User> findById(long userId) {
        return jdbc.query("SELECT " + COLUMNS + " FROM USERS WHERE USER_ID = ?", ROW_MAPPER, userId)
                .stream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT " + COLUMNS + " FROM USERS WHERE EMAIL = ?", ROW_MAPPER, email)
                .stream().findFirst();
    }

    public List<User> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM USERS ORDER BY USER_ID", ROW_MAPPER);
    }

    /** 이메일·비밀번호 해시·역할을 수정하고 바뀐 행 수를 돌려줌 */
    public int update(User user) {
        return jdbc.update(
                "UPDATE USERS SET EMAIL = ?, PASSWORD_HASH = ?, ROLE = ? WHERE USER_ID = ?",
                user.email(), user.passwordHash(), user.role(), user.userId());
    }

    public int deleteById(long userId) {
        return jdbc.update("DELETE FROM USERS WHERE USER_ID = ?", userId);
    }

    private static User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("USER_ID"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD_HASH"),
                rs.getString("ROLE"),
                JdbcSupport.localDate(rs, "CREATED_AT"));
    }
}
