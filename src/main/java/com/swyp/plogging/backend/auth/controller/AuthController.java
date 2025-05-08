package com.swyp.plogging.backend.auth.controller;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.controller.dto.TokenRefreshRequest;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;

    // 현재 인증된 사용자 정보 반환
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        if (customOAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Not authenticated"));
        }

        AppUser user = customOAuth2User.getAppUser();
        Map<String, Object> userInfo = Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "region", user.getRegion(),
                "profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "",
                "registered", user.isRegistered(),
                "provider", user.getAuthProvider().toString()
        );

        return ResponseEntity.ok(Map.of("success", true, "user", userInfo));
    }

    // 사용자 정보 업데이트 (추가 정보 입력 시)
    @PostMapping("/register")
    public ResponseEntity<?> completeRegistration(
            @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @RequestBody Map<String, String> request) {

        if (customOAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Not authenticated"));
        }

        AppUser user = customOAuth2User.getAppUser();
        String nickname = request.get("nickname");
        String region = request.get("region");

        user.update(nickname, region, null);
        user.completeRegistration();
        appUserRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registration completed"));
    }

    // 로그인 상태 확인
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(@AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        boolean isAuthenticated = customOAuth2User != null;
        return ResponseEntity.ok(Map.of(
                "success", true,
                "authenticated", isAuthenticated
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 세션 기반 인증 로그아웃
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 쿠키 기반 인증 로그아웃
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    cookie.setValue("");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        // JWT 구현 시 사용할 토큰 갱신 로직
        // 현재는 더미 응답만 반환
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "토큰이 갱신되었습니다.",
                "token", "dummy_new_token"
        ));
    }
}
