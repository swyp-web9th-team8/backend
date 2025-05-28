package com.swyp.plogging.backend.auth.controller;

import com.swyp.plogging.backend.auth.controller.dto.*;
import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.auth.service.AuthService;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.domain.Region;
import com.swyp.plogging.backend.post.repository.RegionRepository;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.UserRegion;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import com.swyp.plogging.backend.user.repository.UserRegionRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final RegionRepository regionRepository;
    private final UserRegionRepository userRegionRepository;
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
        // 이 메서드는 실제로 호출되지 않고 Swagger 문서화 용도로만 사용
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
        // 이 메서드는 실제로 호출되지 않고 Swagger 문서화 용도로만 사용
        // 실제 구글 로그인은 /oauth2/authorization/google 엔드포인트를 통해 이루어짐
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이 엔드포인트는 문서화 용도입니다. 실제 로그인은 /oauth2/authorization/google을 사용하세요."
        ));
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "현재 인증된 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 사용자 정보를 조회함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "user",
                                    value = "{ \"success\": true, \"user\": { \"id\": 1, \"email\": \"user@example.com\" } }"
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
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "세션이 없습니다. 로그인이 필요합니다."));
        }
        
        AppUser user = (AppUser) session.getAttribute("user");
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "사용자 정보가 없습니다. 로그인이 필요합니다."));
        }

        // HashMap을 사용하여 더 많은 필드 추가 가능
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("region", user.getRegion());
        userInfo.put("profileImageUrl", user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "");
        userInfo.put("registered", user.isRegistered());
        userInfo.put("provider", user.getAuthProvider().toString());
        userInfo.put("gender", user.getGender()); // 성별 정보 추가

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

        // 기본 사용자 정보 업데이트
        user.update(request.getNickname(), request.getRegion(), request.getProfileImageUrl());

        // 성별 정보 설정
        user.setGender(request.getGender());

        // Region 엔티티로 지역 정보 저장
        String[] regionParts = request.getRegion().split(" ");
        if (regionParts.length >= 2) {
            String city = regionParts[0];
            String district = regionParts[1];
            String neighborhood = regionParts.length > 2 ? regionParts[2] : "";

            // 해당 지역 찾기
            Optional<Region> regionOptional;

            if (!neighborhood.isEmpty()) {
                regionOptional = regionRepository.findByCityAndDistrictAndNeighborhood(city, district, neighborhood);
                log.info("지역 검색 결과: city={}, district={}, neighborhood={}, 결과={}",
                        city, district, neighborhood, regionOptional.isPresent());
            } else {
                // 동 정보가 없는 경우, 구 정보만으로 검색
                List<Region> regions = regionRepository.findByCityAndDistrict(city, district);
                regionOptional = regions.isEmpty() ? Optional.empty() : Optional.of(regions.get(0));
                log.info("지역 검색 결과: city={}, district={}, 결과={}",
                        city, district, regionOptional.isPresent());
            }

            if (regionOptional.isPresent()) {
                Region region = regionOptional.get();

                // 기존 UserRegion 확인 및 설정 해제
                userRegionRepository.findPrimaryRegionByUserId(user.getId())
                        .ifPresent(existingPrimary -> {
                            existingPrimary.unsetPrimary();
                            userRegionRepository.save(existingPrimary);
                        });

                // 새 UserRegion 생성 및 저장
                UserRegion userRegion = new UserRegion(user, region, true);
                userRegionRepository.save(userRegion);


                log.info("사용자 ID {}: 지역 정보 등록 - Region ID {}, 지역명: {}",
                        user.getId(), region.getId(), region.getCity() + " " + region.getDistrict() +
                                (region.getNeighborhood() != null && !region.getNeighborhood().isEmpty() ?
                                        " " + region.getNeighborhood() : ""));
            } else {
                log.warn("사용자 ID {}: 지역 정보를 찾을 수 없음 - {}", user.getId(), request.getRegion());
            }
        }

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
    public ResponseEntity<?> getAuthStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = session != null && session.getAttribute("user") != null;
        
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

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawUser(@AuthenticationPrincipal OAuth2User oAuth2User){
        AppUser user = SecurityUtils.getUserOrThrow(oAuth2User);
        try{
            authService.unlink(user);
            return ResponseEntity.ok("Success withdraw" + user.getAuthProvider() +" account");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}