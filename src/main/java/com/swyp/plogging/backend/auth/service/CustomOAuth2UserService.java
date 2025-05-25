package com.swyp.plogging.backend.auth.service;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        try {
            log.info("OAuth2 로그인 시도: 제공자 = {}", request.getClientRegistration().getRegistrationId());

            OAuth2User oAuth2User = super.loadUser(request);

            String registrationId = request.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // 디버깅: OAuth 제공자로부터 받은 속성 확인
            log.debug("OAuth2 제공자({})로부터 받은 속성: {}", registrationId, attributes);

            // AuthProvider는 미리 얻어둠
            AuthProvider provider = AuthProvider.valueOf(registrationId.toUpperCase());

            // 소셜 로그인 제공자에 따라 사용자 정보 추출 및 저장 로직을 분리
            AppUser appUser;

            if ("kakao".equals(registrationId.toLowerCase())) {
                // 카카오 로그인
                appUser = processKakaoUser(attributes, provider);
            } else {
                // 구글 로그인
                appUser = processGoogleUser(attributes, provider);
            }

            log.info("OAuth2 로그인 성공: 제공자 = {}, 이메일 = {}, 등록 상태 = {}",
                provider, appUser.getEmail(), appUser.isRegistered());
            return new CustomOAuth2User(appUser, attributes);
        } catch (Exception e) {
            log.error("OAuth2 로그인 처리 중 오류 발생: " + e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("processing_error"), "사용자 정보 처리 실패: " + e.getMessage(), e);
        }
    }

    // 카카오 로그인 처리
    private AppUser processKakaoUser(Map<String, Object> attributes, AuthProvider provider) {
        Long kakaoId = (Long) attributes.get("id");
        if (kakaoId == null) {
            throw new IllegalArgumentException("카카오 ID가 없습니다.");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        // kakaoAccount가 null인 경우 처리
        if (kakaoAccount == null) {
            log.warn("카카오 계정 정보가 없습니다. ID: {}", kakaoId);
            // 기본값 설정
            return findOrCreateUser(
                "kakao_" + kakaoId + "@placeholder.com",
                "카카오사용자",
                null,
                provider
            );
        }

        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        // 프로필이 null인 경우 처리
        if (kakaoProfile == null) {
            log.warn("카카오 프로필 정보가 없습니다. ID: {}", kakaoId);
            return findOrCreateUser(
                "kakao_" + kakaoId + "@placeholder.com",
                "카카오사용자",
                null,
                provider
            );
        }

        // 이메일 설정
        String email = "kakao_" + kakaoId + "@placeholder.com";

        // 카카오 계정에서 이메일이 제공되는 경우 사용
        if (kakaoAccount.containsKey("email")) {
            Boolean isEmailValid = (Boolean) kakaoAccount.getOrDefault("is_email_valid", false);
            Boolean isEmailVerified = (Boolean) kakaoAccount.getOrDefault("is_email_verified", false);

            if (Boolean.TRUE.equals(isEmailValid) && Boolean.TRUE.equals(isEmailVerified)) {
                email = (String) kakaoAccount.get("email");
                log.info("카카오 계정에서 유효한 이메일을 가져왔습니다: {}", email);
            }
        }

        // 닉네임과 프로필 이미지
        String name = (String) kakaoProfile.get("nickname");
        String pictureUrl = (String) kakaoProfile.get("profile_image_url");

        return findOrCreateUser(email, name, pictureUrl, provider);
    }

    // 구글 로그인 처리
    private AppUser processGoogleUser(Map<String, Object> attributes, AuthProvider provider) {
        String email = (String) attributes.get("email");
        if (email == null) {
            throw new IllegalArgumentException("구글 이메일이 없습니다.");
        }

        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");

        log.warn("================================================");
        log.warn("email: {}", email);
        log.warn("name: {}", name);
        log.warn("picture (profileImageUrl): {}", pictureUrl);
        attributes.forEach((key, value) -> log.warn("attr -> {} : {}", key, value));
        log.warn("================================================");

        return findOrCreateUser(email, name, pictureUrl, provider);
    }

    // 사용자 찾기 또는 생성
    private AppUser findOrCreateUser(String email, String name, String pictureUrl, AuthProvider provider) {
        return appUserRepository.findByEmail(email)
            .map(existingUser -> {
                log.info("기존 사용자 발견: 이메일 = {}, 등록 상태 = {}", email, existingUser.isRegistered());

                // 기존 사용자 정보 업데이트 (선택적)
                boolean updated = false;

                // 이름이 변경되었으면 업데이트
                if (name != null && !name.equals(existingUser.getNickname())) {
                    existingUser.updateNickname(name);
                    updated = true;
                    log.debug("사용자 닉네임 업데이트: {} -> {}", existingUser.getNickname(), name);
                }

                // 프로필 이미지 업데이트하지 않음 - 사용자가 직접 변경한 이미지 유지
                // 로그인 시마다 OAuth 제공자의 이미지로 덮어쓰지 않도록 수정
                if (pictureUrl != null && existingUser.getProfileImageUrl() == null) {
                    // 프로필 이미지가 아직 없는 경우에만 설정
                    existingUser.updateProfileImageUrl(pictureUrl);
                    updated = true;
                    log.debug("초기 프로필 이미지 설정: {}", pictureUrl);
                }

                // 변경사항이 있으면 저장
                if (updated) {
                    log.info("사용자 정보 업데이트: 이메일 = {}", email);
                    return appUserRepository.save(existingUser);
                }

                return existingUser;
            })
            .orElseGet(() -> {
                // 새 사용자 생성
                log.info("새 사용자 생성: 이메일 = {}, 소셜 제공자 = {}", email, provider);
                String nickname = name != null ? name : "사용자";
                String region = "서울"; // 기본 지역

                // 새 사용자 생성 - 처음에는 등록되지 않은 상태로 생성
                // AppUser.newInstance 메서드는 이미 registered=false로 설정함
                AppUser newUser = AppUser.newInstance(email, nickname, region, provider, pictureUrl);

                AppUser savedUser = appUserRepository.save(newUser);
                log.info("새 사용자가 생성되었습니다: ID = {}, 이메일 = {}, 등록 상태 = {}",
                    savedUser.getId(), savedUser.getEmail(), savedUser.isRegistered());
                return savedUser;
            });
    }
}