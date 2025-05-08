package com.swyp.plogging.backend.user.service;

import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;
import com.swyp.plogging.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public ProfileResponse getProfile(Long userId) {
        return userRepository.findProfileByUserId(userId);
    }
}
