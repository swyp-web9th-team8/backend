package com.swyp.plogging.backend.user.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/auth")
public class OAuthCallbackController {

    @GetMapping("/google/callback")
    public String handleGoogleCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {

        // 오류 파라미터 체크
        if (error != null) {
            log.error("Google OAuth 오류: {}", error);
            return "redirect:/login?error=" + error;
        }

        // 코드 파라미터 체크
        if (code == null) {
            log.error("Google 인증 코드가 없습니다.");
            return "redirect:/login?error=no_code";
        }

        try {
            log.info("Google 콜백 수신: code={}", code);

            // Spring Security 표준 OAuth2 경로로 리다이렉트
            String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8.toString());
            return "redirect:/login/oauth2/code/google?code=" + encodedCode;
        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩 오류", e);
            return "redirect:/login?error=encoding_error";
        } catch (Exception e) {
            log.error("Google 콜백 처리 중 오류 발생", e);
            return "redirect:/login?error=callback_error";
        }
    }

    @GetMapping("/kakao/callback")
    public String handleKakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "error", required = false) String error) {

        // 오류 파라미터 체크
        if (error != null) {
            log.error("Kakao OAuth 오류: {}", error);
            return "redirect:/login?error=" + error;
        }

        // 코드 파라미터 체크
        if (code == null) {
            log.error("Kakao 인증 코드가 없습니다.");
            return "redirect:/login?error=no_code";
        }

        try {
            log.info("Kakao 콜백 수신: code={}", code);

            // Spring Security 표준 OAuth2 경로로 리다이렉트
            String encodedCode = URLEncoder.encode(code, StandardCharsets.UTF_8.toString());
            return "redirect:/login/oauth2/code/kakao?code=" + encodedCode;
        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩 오류", e);
            return "redirect:/login?error=encoding_error";
        } catch (Exception e) {
            log.error("Kakao 콜백 처리 중 오류 발생", e);
            return "redirect:/login?error=callback_error";
        }
    }

    // 디버깅용 엔드포인트
    @GetMapping("/debug")
    @ResponseBody
    public Map<String, Object> debugEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "OAuthCallbackController is working");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}