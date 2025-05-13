package com.swyp.plogging.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        AppUser user = oAuth2User.getAppUser();

        log.info("OAuth2 로그인 성공: 사용자 ID = {}, 이메일 = {}, 제공자 = {}",
                user.getId(), user.getEmail(), user.getAuthProvider());

        // 사용자의 등록 상태에 따라 다른 페이지로 리다이렉트
        if (user.isRegistered()) {
            // 등록된 사용자는 홈 화면으로 리다이렉트
            log.info("등록된 사용자 - 홈 화면으로 리다이렉트: {}", user.getId());
            response.sendRedirect("/home");
        } else {
            // 미등록 사용자는 추가 정보 입력 페이지로 리다이렉트
            log.info("미등록 사용자 - 회원가입 화면으로 리다이렉트: {}", user.getId());
            response.sendRedirect("/register");
        }
    }
}