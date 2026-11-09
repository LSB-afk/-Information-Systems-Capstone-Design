package com.red.jejucalendar.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** SCR-C-001 로그인, SCR-C-000 로그아웃 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String role, String email, String password) {
    }

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginUser login(@RequestBody LoginRequest request, HttpServletRequest http) {
        LoginUser user = authService.login(request.role(), request.email(), request.password());
        HttpSession old = http.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        http.getSession(true).setAttribute(LoginUser.SESSION_KEY, user);
        return user;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http) {
        HttpSession session = http.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    /** 현재 로그인 사용자 (공통 레이아웃의 계정 영역·메뉴 표시에 사용) */
    @GetMapping("/me")
    public LoginUser me(@CurrentUser LoginUser user) {
        return user;
    }
}
