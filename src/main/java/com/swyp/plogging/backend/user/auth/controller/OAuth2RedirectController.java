package com.swyp.plogging.backend.user.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/oauth2")
@Tag(name = "OAuth2 Redirect", description = "OAuth2 리다이렉트 엔드포인트")
public class OAuth2RedirectController {

    @Operation(summary = "카카오 로그인 리다이렉트", description = "카카오 OAuth2 인증을 위한 리다이렉트 엔드포인트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "카카오 로그인 페이지로 리다이렉트")
    })
    @GetMapping("/authorization/kakao")
    public String kakaoRedirect() {
        // 실제 리다이렉트는 Spring Security에서 처리
        return "redirect:/oauth2/authorization/kakao";
    }

    @Operation(summary = "구글 로그인 리다이렉트", description = "구글 OAuth2 인증을 위한 리다이렉트 엔드포인트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "구글 로그인 페이지로 리다이렉트")
    })
    @GetMapping("/authorization/google")
    public String googleRedirect() {
        // 실제 리다이렉트는 Spring Security에서 처리
        return "redirect:/oauth2/authorization/google";
    }
}
