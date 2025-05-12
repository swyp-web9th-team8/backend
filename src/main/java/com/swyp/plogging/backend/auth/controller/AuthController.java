package com.swyp.plogging.backend.auth.controller;

import com.swyp.plogging.backend.auth.controller.dto.*;
import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.auth.service.AuthService;
import com.swyp.plogging.backend.controller.dto.TokenRefreshRequest;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final AuthService authService;

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 OAuth2 인증을 시작합니다. 이 엔드포인트는 카카오 로그인 페이지로 리다이렉트됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "카카오 로그인 페이지로 리다이렉트",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RedirectResponse.class),
                            examples = @ExampleObject(
                                    name = "redirect",
                                    value = "{ \"redirectUrl\": \"https://kauth.kakao.com/oauth/authorize?client_id=...\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 시 사용자 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginSuccessResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = "{ \"success\": true, \"user\": { \"id\": 1, \"email\": \"kakao_12345@placeholder.com\", \"nickname\": \"홍길동\", \"region\": \"서울시 강남구\", \"profileImageUrl\": \"https://example.com/profile.jpg\", \"registered\": true, \"provider\": \"KAKAO\" } }"
                            )
                    )
            )
    })
    @SecurityRequirement(name = "kakao_oauth")
    @GetMapping("/kakao")
    public ResponseEntity<Map<String, Object>> kakaoLogin() {
        // 이 메서드는 실제로 호출되지 않고 Swagger 문서화 용도로만 사용됨
        // 실제 카카오 로그인은 /oauth2/authorization/kakao 엔드포인트를 통해 이루어짐
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이 엔드포인트는 문서화 용도입니다. 실제 로그인은 /oauth2/authorization/kakao를 사용하세요."
        ));
    }

    @Operation(
            summary = "구글 로그인",
            description = "구글 OAuth2 인증을 시작합니다. 이 엔드포인트는 구글 로그인 페이지로 리다이렉트됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "302",
                    description = "구글 로그인 페이지로 리다이렉트",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RedirectResponse.class),
                            examples = @ExampleObject(
                                    name = "redirect",
                                    value = "{ \"redirectUrl\": \"https://accounts.google.com/o/oauth2/v2/auth?client_id=...\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공 시 사용자 정보 반환",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginSuccessResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = "{ \"success\": true, \"user\": { \"id\": 2, \"email\": \"user@gmail.com\", \"nickname\": \"홍길동\", \"region\": \"서울시 강남구\", \"profileImageUrl\": \"https://example.com/profile.jpg\", \"registered\": true, \"provider\": \"GOOGLE\" } }"
                            )
                    )
            )
    })
    @SecurityRequirement(name = "google_oauth")
    @GetMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin() {
        // 이 메서드는 실제로 호출되지 않고 Swagger 문서화 용도로만 사용됨
        // 실제 구글 로그인은 /oauth2/authorization/google 엔드포인트를 통해 이루어짐
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이 엔드포인트는 문서화 용도입니다. 실제 로그인은 /oauth2/authorization/google을 사용하세요."
        ));
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 인증된 사용자의 상세 정보를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 사용자 정보를 조회함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginSuccessResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = "{ \"success\": true, \"user\": { \"id\": 1, \"email\": \"user@example.com\", \"nickname\": \"홍길동\", \"region\": \"서울시 강남구\", \"profileImageUrl\": \"https://example.com/profile.jpg\", \"registered\": true, \"provider\": \"KAKAO\" } }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "error",
                                    value = "{ \"success\": false, \"error\": \"Not authenticated\" }"
                            )
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
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

    @Operation(summary = "사용자 정보 업데이트", description = "OAuth2 인증 후 추가 사용자 정보를 등록하고 회원가입을 완료합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 사용자 정보를 업데이트함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegistrationResponse.class),
                            examples = @ExampleObject(
                                    name = "success",
                                    value = "{ \"success\": true, \"message\": \"Registration completed\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "유효하지 않은 요청 (예: 중복된 닉네임)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "error",
                                    value = "{ \"success\": false, \"error\": \"이미 사용중인 닉네임입니다.\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "error",
                                    value = "{ \"success\": false, \"error\": \"Not authenticated\" }"
                            )
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> completeRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User customOAuth2User,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "사용자 등록 정보",
                    content = @Content(schema = @Schema(implementation = SignupRequest.class)))
            @Valid @RequestBody SignupRequest request) {

        if (customOAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Not authenticated"));
        }

        AppUser user = customOAuth2User.getAppUser();

        // 닉네임 중복 확인
        if (appUserRepository.findByNickname(request.getNickname()).isPresent() &&
                !user.getNickname().equals(request.getNickname())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "이미 사용중인 닉네임입니다."));
        }

        // 사용자 정보 업데이트
        user.update(request.getNickname(), request.getRegion(), request.getProfileImageUrl());

        // 성별 정보 설정 (gender 필드 추가 필요)
        user.setGender(request.getGender());

        user.completeRegistration();
        appUserRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Registration completed"));
    }

    @Operation(summary = "로그인 상태 확인", description = "현재 사용자의 인증 상태를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 인증 상태를 조회함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthStatusResponse.class),
                            examples = @ExampleObject(
                                    name = "status",
                                    value = "{ \"success\": true, \"authenticated\": true }"
                            )
                    )
            )
    })
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User customOAuth2User) {
        boolean isAuthenticated = customOAuth2User != null;
        return ResponseEntity.ok(Map.of(
                "success", true,
                "authenticated", isAuthenticated
        ));
    }

    @Operation(summary = "로그아웃", description = "현재 세션을 로그아웃 상태로 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 로그아웃함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LogoutResponse.class),
                            examples = @ExampleObject(
                                    name = "logout",
                                    value = "{ \"success\": true, \"message\": \"Logged out successfully\" }"
                            )
                    )
            )
    })
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

    @Operation(summary = "토큰 갱신", description = "인증 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 토큰을 갱신함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenRefreshResponse.class),
                            examples = @ExampleObject(
                                    name = "refresh",
                                    value = "{ \"success\": true, \"message\": \"토큰이 갱신되었습니다.\", \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\" }"
                            )
                    )
            )
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "토큰 갱신 요청",
                    content = @Content(schema = @Schema(implementation = TokenRefreshRequest.class)))
            @RequestBody TokenRefreshRequest request) {
        // JWT 구현 시 사용할 토큰 갱신 로직
        // 현재는 더미 응답만 반환
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "토큰이 갱신되었습니다.",
                "token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        ));
    }
}
