package com.swyp.plogging.backend.user.controller;

import com.swyp.plogging.backend.common.dto.ApiResponse;
import com.swyp.plogging.backend.common.util.SecurityUtils;
import com.swyp.plogging.backend.user.controller.dto.EditableProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UpdateProfileRequest;
import com.swyp.plogging.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PatchMapping("/profile-detail")
    public ApiResponse<Long> updateProfile(@RequestBody UpdateProfileRequest request, @AuthenticationPrincipal OAuth2User principal) {
        Long currentUserId = SecurityUtils.getUserId(principal);
        userService.updateProfile(currentUserId, request);

        return ApiResponse.ok(currentUserId, String.format("Profile updated successfully! User ID = %d", currentUserId));
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
