package com.swyp.plogging.backend.user.controller;

import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.user.controller.dto.EditableProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UpdateNicknameRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdatePhoneNumRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdateRegionRequest;
import com.swyp.plogging.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{userId}/rankings")
    public String fetchRankingOfUser(@PathVariable(name = "userId") Long userId) {
        return "Successfully retrieved rankings and badges.";
    }


    @GetMapping("/{userId}/participated-posts")
    public String fetchParticipatedPostsOfUser(@PathVariable(name = "userId") Long userId) {
        return "Successfully retrieved participated events.";
    }

    @GetMapping("/{userId}/created-posts")
    public String fetchCreatedPsotsOfUser(@PathVariable(name = "userId") Long userId) {
        return "Successfully retrieved created events.";
    }
}
