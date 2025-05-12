package com.swyp.plogging.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plogging API",
                version = "v1.0.0",
                description = "Plogging 애플리케이션의 API 문서입니다."
        ),
        servers = {
                @Server(url = "${app.baseUrl}", description = "Production Server")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "kakao_oauth",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(
                        authorizationCode = @OAuthFlow(
                                authorizationUrl = "${spring.security.oauth2.client.provider.kakao.authorization-uri}",
                                tokenUrl = "${spring.security.oauth2.client.provider.kakao.token-uri}",
                                scopes = {
                                        @OAuthScope(name = "profile_nickname", description = "닉네임 정보"),
                                        @OAuthScope(name = "profile_image", description = "프로필 이미지 정보")
                                }
                        )
                )
        ),
        @SecurityScheme(
                name = "google_oauth",
                type = SecuritySchemeType.OAUTH2,
                flows = @OAuthFlows(
                        authorizationCode = @OAuthFlow(
                                authorizationUrl = "${spring.security.oauth2.client.provider.google.authorization-uri}",
                                tokenUrl = "${spring.security.oauth2.client.provider.google.token-uri}",
                                scopes = {
                                        @OAuthScope(name = "profile", description = "프로필 정보"),
                                        @OAuthScope(name = "email", description = "이메일 정보")
                                }
                        )
                )
        )
})
public class OpenAPIConfig {
}
