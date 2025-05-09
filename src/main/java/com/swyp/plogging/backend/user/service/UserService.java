package com.swyp.plogging.backend.user.service;

import com.swyp.plogging.backend.common.exception.UnsupportedUpdateRequestException;
import com.swyp.plogging.backend.common.exception.UserNotFoundException;
import com.swyp.plogging.backend.common.service.FileService;
import com.swyp.plogging.backend.user.controller.dto.EditableProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.controller.dto.UpdateNicknameRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdatePhoneNumRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdateProfileRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdatePushEnabledRequest;
import com.swyp.plogging.backend.user.controller.dto.UpdateRegionRequest;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileService fileService;

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

        if (request instanceof UpdateNicknameRequest nicknameRequest) {
            user.updateNickname(nicknameRequest.getNickname());
        } else if (request instanceof UpdateRegionRequest regionRequest) {
            user.updateRegion(regionRequest.getRegion());
        } else if (request instanceof UpdatePhoneNumRequest phoneRequest) {
            user.updatePhoneNum(phoneRequest.getPhoneNum());
        } else if (request instanceof UpdatePushEnabledRequest pushRequest) {
            user.updatePushEnabled(pushRequest.getPushEnabled());
        } else {
            throw new UnsupportedUpdateRequestException();
        }
    }

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        String filename = fileService.uploadImageAndGetFileName(file);

        AppUser user = getUser(userId);
        String publicPath = "/images/" + filename;
        user.updateProfileImageUrl(publicPath);

        return publicPath;
    }
}
