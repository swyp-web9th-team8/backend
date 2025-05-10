package com.swyp.plogging.backend.auth.service;

import com.swyp.plogging.backend.auth.controller.dto.SignupRequest;
import com.swyp.plogging.backend.common.exception.CustomException;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;

    @Transactional
    public AppUser completeSignup(Long userId, SignupRequest request) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 닉네임 중복 확인
        if (appUserRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new CustomException("이미 사용중인 닉네임입니다.", HttpStatus.BAD_REQUEST);
        }

        // 사용자 정보 업데이트
        user.update(request.getNickname(), request.getRegion(), request.getProfileImageUrl());

        // 성별 정보 업데이트 (gender 필드 추가 필요)
        user.setGender(request.getGender());

        // 회원가입 완료 상태로 설정
        user.completeRegistration();

        return appUserRepository.save(user);
    }
}