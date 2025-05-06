package com.swyp.plogging.backend.common.util;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SecurityUtil {

    public static AppUser getUserOrThrow(OAuth2User principal) {
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getAppUser();
        }

        throw new UnauthorizedUserException();
    }

}
