package com.swyp.plogging.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp.plogging.backend.auth.handler.OAuth2LoginSuccessHandler;
import com.swyp.plogging.backend.auth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Value("${app.frontendUrl}")
    private String frontendUrl;

    @Value("${app.baseUrl}")
    private String baseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 현재 활성화된 프로필 로깅
        log.info("활성화된 프로필: {}", Arrays.toString(environment.getActiveProfiles()));
        log.info("baseUrl 설정: {}", baseUrl);
        log.info("frontendUrl 설정: {}", frontendUrl);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize ->
                        authorize
                                // 공개 경로 설정
                                .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                                // Swagger UI 접근 허용
                                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                                // API 개발을 위한 접근 허용
                                .requestMatchers("/api/**").permitAll()
                                // OAuth 관련 경로 허용
                                .requestMatchers("/oauth2/**", "/login/**", "/api/auth/**").permitAll()
                                // 나머지 요청은 인증 필요 (개발 완료 후 활성화)
                                //.anyRequest().authenticated()
                                .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 로그인 실패", exception);

                            // 에러 원인 로깅
                            String errorMessage = "인증 실패";
                            if (exception.getCause() != null) {
                                errorMessage = exception.getCause().getMessage();
                                log.error("원인: {}", errorMessage);
                            }

                            // JSON 응답 vs 리다이렉트 결정
                            String acceptHeader = request.getHeader("Accept");
                            if (acceptHeader != null && acceptHeader.contains("application/json")) {
                                // API 요청에는 JSON 응답
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");

                                Map<String, Object> errorResponse = new HashMap<>();
                                errorResponse.put("success", false);
                                errorResponse.put("error", "인증 실패: " + exception.getMessage());

                                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                            } else {
                                // 브라우저 요청에는 리다이렉트
                                String redirectUrl = frontendUrl + "/login?error=oauth&message=" +
                                        exception.getMessage().replaceAll("\\s+", "+");
                                response.sendRedirect(redirectUrl);
                            }
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 개발 환경과 프로덕션 환경 모두 허용
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:3000",
                "https://ploggo.co.kr",
                "https://api.ploggo.co.kr"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuditorAware<String> auditorAware() {
        return new AuditorAwareImpl();
    }
}