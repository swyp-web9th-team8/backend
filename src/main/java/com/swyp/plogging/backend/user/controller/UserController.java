package com.swyp.plogging.backend.user.controller;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.common.dto.ApiPagedResponse;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.participation.dto.MyPostResponse;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.user.controller.dto.*;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.UserRegion;
import com.swyp.plogging.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ParticipationService participationService;

    @GetMapping("/profile")
    public ApiResponse<ProfileResponse> getProfile(@AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        ProfileResponse profile = userService.getProfile(currentUserId);

        return ApiResponse.ok(profile, "Profile fetched successfully!");
    }

    @GetMapping("/profile-detail")
    public ApiResponse<EditableProfileResponse> getEditableProfile(@AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        EditableProfileResponse editableProfile = userService.getEditableProfile(currentUserId);

        return ApiResponse.ok(editableProfile, "Editable Profile fetched successfully!");
    }

    @PostMapping("/profile-image")
    public ApiResponse<String> uploadProfileImage(@AuthenticationPrincipal OAuth2User principal, @RequestParam("file") MultipartFile file) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        String url = userService.uploadProfileImage(currentUserId, file);

        return ApiResponse.ok(url, "Profile image updated successfully!");
    }

    @PostMapping("/nickname")
    public ApiResponse<Long> updateNickname(@RequestBody UpdateNicknameRequest request, @AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.updateProfile(currentUserId, request);
        return ApiResponse.ok(currentUserId, String.format("Profile nickname updated successfully! User ID = %d", currentUserId));
    }

    @PostMapping("/region")
    public ApiResponse<Long> updateRegion(@RequestBody UpdateRegionRequest request, @AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.updateProfile(currentUserId, request);
        return ApiResponse.ok(currentUserId, String.format("Profile region updated successfully! User ID = %d", currentUserId));
    }

    @PostMapping("/phone-number")
    public ApiResponse<Long> updatePhoneNumber(@RequestBody UpdatePhoneNumRequest request, @AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.updateProfile(currentUserId, request);
        return ApiResponse.ok(currentUserId, String.format("Profile phonNumber updated successfully! User ID = %d", currentUserId));
    }

    @GetMapping("/participated-posts")
    public ApiPagedResponse<MyPostResponse> fetchParticipatedPostsOfUser(@AuthenticationPrincipal OAuth2User principal,
                                                                         @PageableDefault Pageable pageable) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        Page<MyPostResponse> participatedPosts = participationService.getParticipatedPosts(currentUserId, pageable);

        return ApiPagedResponse.ok(participatedPosts, "Participated posts fetched successfully!");
    }

    @GetMapping("/created-posts")
    public ApiPagedResponse<MyPostResponse> fetchCreatedPsotsOfUser(@AuthenticationPrincipal OAuth2User principal,
                                                                    @PageableDefault Pageable pageable) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        Page<MyPostResponse> createdPosts = participationService.getCreatedPosts(currentUserId, pageable);

        return ApiPagedResponse.ok(createdPosts, "Created posts fetched successfully!");
    }

    @GetMapping("/badges")
    public ApiResponse<UserBadgesResponse> getUserBadges(@AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        UserBadgesResponse userBadges = userService.getUserBadges(currentUserId);

        return ApiResponse.ok(userBadges, "User badges fetched successfully!");
    }

    // --- 새로운 UserRegion 관련 엔드포인트 ---

    // 사용자 지역 목록 조회
    @GetMapping("/regions")
    public ApiResponse<List<UserRegionResponse>> getUserRegions(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ApiResponse.error("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long currentUserId = SecurityUtils.getUserId(principal);
        List<UserRegion> userRegions = userService.getUserRegions(currentUserId);

        List<UserRegionResponse> response = userRegions.stream()
                .map(ur -> new UserRegionResponse(
                        ur.getId(),
                        ur.getRegion().getId(),
                        ur.getRegion().getCity(),
                        ur.getRegion().getDistrict(),
                        ur.getRegion().getNeighborhood(),
                        ur.isPrimary()
                ))
                .collect(Collectors.toList());

        return ApiResponse.ok(response, "사용자 지역 목록이 성공적으로 조회되었습니다.");
    }

    // 지역 추가
    @PostMapping("/regions")
    public ApiResponse<Void> addUserRegion(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody UserRegionRequest request) {

        if (principal == null) {
            return ApiResponse.error("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.addUserRegion(currentUserId, request.getRegionId(), request.isPrimary());

        return ApiResponse.ok(null, "지역이 성공적으로 추가되었습니다.");
    }

    // 지역 삭제
    @DeleteMapping("/regions/{regionId}")
    public ApiResponse<Void> removeUserRegion(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long regionId) {

        if (principal == null) {
            return ApiResponse.error("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.removeUserRegion(currentUserId, regionId);

        return ApiResponse.ok(null, "지역이 성공적으로 삭제되었습니다.");
    }

    // 기본 지역 변경
    @PatchMapping("/regions/{regionId}/primary")
    public ApiResponse<Void> updatePrimaryRegion(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long regionId) {

        if (principal == null) {
            return ApiResponse.error("인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.updatePrimaryRegion(currentUserId, regionId);

        return ApiResponse.ok(null, "기본 지역이 성공적으로 변경되었습니다.");
    }
}