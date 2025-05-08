package com.swyp.plogging.backend.auth.service;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.user.domain.AppUser;
import com.swyp.plogging.backend.user.domain.AuthProvider;
import com.swyp.plogging.backend.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

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

        return new CustomOAuth2User(appUser, attributes);
    }

    // 카카오 로그인 처리
    private AppUser processKakaoUser(Map<String, Object> attributes, AuthProvider provider) {
        Long kakaoId = (Long) attributes.get("id");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        // 이메일 대신 카카오 ID를 사용한 고유 식별자 생성
        String email = "kakao_" + kakaoId + "@placeholder.com";

        // 닉네임과 프로필 이미지
        String name = (String) kakaoProfile.get("nickname");
        String pictureUrl = (String) kakaoProfile.get("profile_image_url");

        return findOrCreateUser(email, name, pictureUrl, provider);
    }

    // 구글 로그인 처리
    private AppUser processGoogleUser(Map<String, Object> attributes, AuthProvider provider) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String pictureUrl = (String) attributes.get("picture");

        return findOrCreateUser(email, name, pictureUrl, provider);
    }

    // 사용자 찾기 또는 생성
    private AppUser findOrCreateUser(String email, String name, String pictureUrl, AuthProvider provider) {
        return appUserRepository.findByEmail(email)
                .map(existingUser -> {
                    // 기존 사용자는 그대로 반환
                    return existingUser;
                })
                .orElseGet(() -> {
                    // 새 사용자 생성
                    String nickname = name != null ? name : "사용자";
                    String region = "서울";

                    return appUserRepository.save(
                            AppUser.newInstance(email, nickname, region, provider, pictureUrl)
                    );
                });
    }
}