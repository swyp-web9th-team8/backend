package com.swyp.plogging.backend.user.controller;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UserInfoUpdateRequest;
import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(@AuthenticationPrincipal OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            AppUser appUser = customOAuth2User.getAppUser();
            return ResponseEntity.ok(ProfileResponse.of(appUser));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/{userId}/rankings")
    public String fetchRankingOfUser(@PathVariable(name = "userId") Long userId) {
        return "Successfully retrieved rankings and badges.";
    }

    @PatchMapping("/{userId}")
    public String updateUserInfo(@PathVariable(name = "userId") Long userId,
        @RequestBody UserInfoUpdateRequest request) {
        return "User information updated success.";
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
