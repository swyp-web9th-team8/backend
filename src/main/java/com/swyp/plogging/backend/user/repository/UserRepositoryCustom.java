package com.swyp.plogging.backend.user.repository;

import com.swyp.plogging.backend.user.controller.dto.ProfileResponse;

public interface UserRepositoryCustom {

    ProfileResponse findProfileByUserId(Long userId);
}
