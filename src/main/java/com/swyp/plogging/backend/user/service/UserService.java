package com.swyp.plogging.backend.user.service;

import com.swyp.plogging.backend.common.exception.UserNotFoundException;
import com.swyp.plogging.backend.user.controller.dto.EditableProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UpdateProfileRequest;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long userId) {
        return userRepository.findProfileByUserId(userId);
    }

    public EditableProfileResponse getEditableProfile(Long userId) {
        AppUser appUser = getUser(userId);

        return EditableProfileResponse.of(appUser);
    }

    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(String.format("Could not find user with ID: %d", userId)));
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = getUser(userId);
        user.updateProfile(request.getNickname(), request.getRegion(), request.getProfileImageUrl(),
            request.getPhoneNum(), request.getPushEnabled());
    }
}
