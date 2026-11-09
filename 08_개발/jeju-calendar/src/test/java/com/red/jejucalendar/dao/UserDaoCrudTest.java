package com.red.jejucalendar.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.red.jejucalendar.domain.User;

/**
 * USERS CRUD 확인
 * Transactional 이라 테스트가 끝나면 모두 되돌려 DB에 데이터가 남지 않음 (시퀀스 번호만 1 증가)
 */
@SpringBootTest
@Transactional
@EnabledIf("com.red.jejucalendar.support.LocalDb#configured")
class UserDaoCrudTest {

    @Autowired
    UserDao userDao;

    @Test
    void createReadUpdateDelete() {
        long id = userDao.insert(new User(null, "crud-test@red.example", "hash-1", "BUSINESS", LocalDate.now()));

        User saved = userDao.findById(id).orElseThrow();
        assertThat(saved.email()).isEqualTo("crud-test@red.example");
        assertThat(saved.role()).isEqualTo("BUSINESS");
        assertThat(userDao.findByEmail("crud-test@red.example")).isPresent();

        int updated = userDao.update(new User(id, "crud-test@red.example", "hash-2", "SERVICE", null));
        assertThat(updated).isEqualTo(1);
        assertThat(userDao.findById(id).orElseThrow().role()).isEqualTo("SERVICE");

        assertThat(userDao.deleteById(id)).isEqualTo(1);
        assertThat(userDao.findById(id)).isEmpty();
    }
}
