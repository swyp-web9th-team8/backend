package com.swyp.plogging.backend.config;

import com.swyp.plogging.backend.auth.domain.CustomOAuth2User;
import com.swyp.plogging.backend.auth.service.CustomOAuth2UserService;
import com.swyp.plogging.backend.user.domain.AppUser;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 공개 경로 설정
                                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                                // API 개발을 위해 모든 API 경로 허용 (개발 중에만)
                                .requestMatchers("/api/**").permitAll()
                                // OAuth 관련 경로 허용
                                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                                // 나머지 요청은 인증 필요 (개발 완료 후 활성화)
                                //.anyRequest().authenticated()
                                .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler((request, response, authentication) -> {
                            // 로그인 성공 시 JSON 응답 반환 (API 서버용)
                            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                            AppUser user = oAuth2User.getAppUser();

                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(String.format(
                                    "{\"success\":true,\"user\":{\"id\":%d,\"email\":\"%s\",\"nickname\":\"%s\",\"registered\":%b}}",
                                    user.getId(), user.getEmail(), user.getNickname(), user.isRegistered()
                            ));
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"error\":\"Authentication failed\"}");
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://ploggo.co.kr"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}
