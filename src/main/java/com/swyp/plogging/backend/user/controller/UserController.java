package com.swyp.plogging.backend.user.controller;

import com.swyp.plogging.backend.common.dto.ApiPagedResponse;
import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.participation.dto.MyPostResponse;
import com.swyp.plogging.backend.participation.service.ParticipationService;
import com.swyp.plogging.backend.user.controller.dto.EditableProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UpdateNicknameRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdatePhoneNumRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdateRegionRequest;
import com.swyp.plogging.backend.user.controller.dto.UserBadgesResponse;
import com.swyp.plogging.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
