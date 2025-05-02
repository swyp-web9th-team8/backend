package com.swyp.plogging.backend.controller;

import com.swyp.plogging.backend.controller.dto.TokenRefreshRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // OAuth 사용 소셜로그인
    @GetMapping("/{provider}")
    public void oAuthSignIn(@PathVariable(name = "provider") String provider) {

    }

    @PostMapping("/logout")
    public String logout() {
        return "로그아웃 되었습니다.";
    }

    @PostMapping("/token/refresh")
    public String refreshToken(@RequestBody TokenRefreshRequest request) {
        return "로그아웃 되었습니다.";
    }

}
