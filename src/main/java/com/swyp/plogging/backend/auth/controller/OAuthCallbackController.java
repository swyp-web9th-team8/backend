package com.swyp.plogging.backend.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/api/auth")
public class OAuthCallbackController {

    @GetMapping("/google/callback")
    public String handleGoogleCallback(@RequestParam("code") String code) {
        log.info("Google callback received with code: {}", code);
        return "redirect:/login/oauth2/code/google?code=" + code;
    }

    @GetMapping("/kakao/callback")
    public String handleKakaoCallback(@RequestParam("code") String code) {
        log.info("Kakao callback received with code: {}", code);
        return "redirect:/login/oauth2/code/kakao?code=" + code;
    }

}