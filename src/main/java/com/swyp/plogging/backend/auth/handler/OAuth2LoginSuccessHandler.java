package com.swyp.plogging.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Value("${app.frontendUrl}")
    private String frontendUrl;

    @Value("${app.redirectUrl}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.debug("OAuth2 로그인 성공 핸들러 호출됨");
        log.debug("baseUrl: {}, frontendUrl: {}, redirectUrl: {}", baseUrl, frontendUrl, redirectUrl);

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            AppUser user = oAuth2User.getAppUser();

            log.info("OAuth2 로그인 성공: 사용자 ID = {}, 이메일 = {}, 제공자 = {}, 등록 상태 = {}",
                    user.getId(), user.getEmail(), user.getAuthProvider(), user.isRegistered());

            // 세션에 사용자 정보 저장
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("USER_ID", user.getId());
            session.setAttribute("ROLE", "USER");
            
            // JSESSIONID 쿠키는 자동으로 설정됨

            // 디버깅 정보
            String acceptHeader = request.getHeader("Accept");
            log.debug("Request URL: {}", request.getRequestURL());
            log.debug("Request Query String: {}", request.getQueryString());
            log.debug("Accept Header: {}", acceptHeader);
            log.debug("세션 ID: {}", session.getId());

            // API 요청 vs 브라우저 요청 처리 결정
            if (acceptHeader != null && acceptHeader.contains("application/json")) {
                // API 요청에는 JSON 응답
                sendJsonResponse(response, user, session.getId());
            } else {
                // 브라우저 요청에는 프론트엔드로 리다이렉트
                sendRedirect(response, user);
            }
        } catch (Exception e) {
            log.error("로그인 성공 처리 중 오류 발생", e);
            handleError(response, e);
        }
    }

    private void sendJsonResponse(HttpServletResponse response, AppUser user, String sessionId) throws IOException {
        // JSON 응답 생성
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("message", "로그인 성공");
        responseBody.put("sessionId", sessionId);

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("nickname", user.getNickname());
        userData.put("registered", user.isRegistered());
        userData.put("provider", user.getAuthProvider().toString());
        if (user.getProfileImageUrl() != null) {
            userData.put("profileImageUrl", user.getProfileImageUrl());
        }

        responseBody.put("user", userData);

        // JSON 응답 전송
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }

    private void sendRedirect(HttpServletResponse response, AppUser user) throws IOException {
        try {
            // 최소한의 정보만 URL에 포함 (보안 강화)
            String redirectPath = frontendUrl + "/oauth/callback" +
                    "?success=true" +
                    "&registered=" + user.isRegistered();
            
            // 세션은 이미 생성되어 있으므로 JSESSIONID 쿠키가 자동으로 전송됨
            // 추가 사용자 정보는 /api/auth/me 엔드포인트를 통해 가져올 수 있음
            
            log.info("프론트엔드로 리다이렉트합니다: {}", redirectPath);
            response.sendRedirect(redirectPath);
        } catch (Exception e) {
            log.error("리다이렉트 처리 중 오류", e);
            // 오류 시 기본 리다이렉트
            String fallbackUrl = frontendUrl + "/oauth/callback?success=true";
            log.info("오류로 기본 URL로 리다이렉트합니다: {}", fallbackUrl);
            response.sendRedirect(fallbackUrl);
        }
    }

    private void handleError(HttpServletResponse response, Exception e) throws IOException {
        // 오류 응답 전송
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다.");
        errorResponse.put("error", e.getMessage());

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}