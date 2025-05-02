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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String email = oAuth2User.getAttribute("email");
        String provider = request.getClientRegistration().getRegistrationId();

        AppUser appUser = appUserRepository.findByEmail(email).orElseGet(() ->
            appUserRepository.save(
                AppUser.newInstance(email, "defaultNickname", "Seoul", AuthProvider.valueOf(provider.toUpperCase()))));

        return new CustomOAuth2User(appUser, oAuth2User.getAttributes());
    }
}
