package com.swyp.plogging.backend.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.plogging.backend.auth.controller.dto.SignupRequest;
import com.swyp.plogging.backend.common.exception.CustomException;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository appUserRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

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

    public boolean unlink(AppUser user) {
        AuthProvider provider = user.getAuthProvider();
        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(provider.toString().toLowerCase(), String.valueOf(user.getId()));
        String token = client.getAccessToken().getTokenValue();
        if (provider.equals(AuthProvider.KAKAO)) {
            return unlinkFromKakao(token);
        } else {
            return unlinkFromGoogle(token, user);
        }
    }

    @Transactional
    public boolean unlinkFromKakao(String accessToken) {
        String url = "https://kapi.kakao.com/v1/user/unlink";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                Map body = objectMapper.readValue(response.getBody(), Map.class);
                String kakaoId = body.get("id").toString();
                String email = "kakao_" + kakaoId + "@placeholder.com";
                AppUser user = appUserRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("일치하는 사용자가 없습니다."));
                user.inActive();
                return true;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            throw new RuntimeException(response.getStatusCode() + " / " + response.getBody());
        }
    }

    @Transactional
    public boolean unlinkFromGoogle(String accessToken, AppUser user) {
        String url = "https://oauth2.googleapis.com/revoke?token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            AppUser inActiveUser = appUserRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("일치하는 사용자가 없습니다."));
            inActiveUser.inActive();
            return true;
        } else {
            throw new RuntimeException(response.getStatusCode() + " / " + response.getBody());
        }
    }
}