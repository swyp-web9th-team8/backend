package com.swyp.plogging.backend.common.util;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.common.exception.UnauthorizedUserException;
import com.swyp.plogging.backend.user.domain.AppUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class... do not instantiate");
    }

    public static AppUser getUserOrThrow(OAuth2User principal) {
        if (principal == null) {
            throw new UnauthorizedUserException("OAuth2User is null (unauthenticated)");
        }

        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getAppUser();
        }

        throw new UnauthorizedUserException("Unexpected principal type: " + principal.getClass().getSimpleName());
    }

    public static Long getUserId(OAuth2User principal) {
        return getUserOrThrow(principal).getId();
    }
}
