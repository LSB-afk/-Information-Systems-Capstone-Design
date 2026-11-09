package com.red.jejucalendar.auth;

import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.red.jejucalendar.common.ApiException;
import com.red.jejucalendar.common.Codes;
import com.red.jejucalendar.dao.UserDao;
import com.red.jejucalendar.domain.User;

/** SCR-C-001 로그인: 역할 선택 + 이메일 + 비밀번호로 계정과 역할을 함께 인증 */
@Service
public class AuthService {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    /** 계정이 없을 때도 같은 시간만큼 비교해 계정 존재 여부가 드러나지 않게 하는 해시 */
    private static final String DUMMY_HASH = new BCryptPasswordEncoder().encode("no-such-account");

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginUser login(String role, String email, String password) {
        if (role == null || !Codes.ROLES.contains(role)) {
            throw ApiException.badRequest("role", "역할을 선택해 주세요.");
        }
        String normalizedEmail = email == null ? "" : email.strip().toLowerCase();
        if (normalizedEmail.isEmpty() || !EMAIL.matcher(normalizedEmail).matches()) {
            throw ApiException.badRequest("email", "올바른 이메일 주소를 입력해 주세요.");
        }
        if (password == null || password.isEmpty()) {
            throw ApiException.badRequest("password", "비밀번호를 입력해 주세요.");
        }

        User user = userDao.findByEmail(normalizedEmail).orElse(null);
        String hash = user == null ? DUMMY_HASH : user.passwordHash();
        boolean passwordMatches = passwordEncoder.matches(password, hash);
        if (user == null || !passwordMatches || !role.equals(user.role())) {
            throw ApiException.unauthorized("이메일 또는 비밀번호를 확인해 주세요.");
        }
        return new LoginUser(user.userId(), user.email(), user.role());
    }
}
