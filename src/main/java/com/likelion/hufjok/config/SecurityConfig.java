package com.likelion.hufjok.config;

import com.likelion.hufjok.security.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2LoginSuccessHandler customSuccessHandler;
    private final CustomOAuth2AuthenticationFailureHandler customFailureHandler;

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 계정 선택을 해야하는데 계속 자동으로 로그인되어서 코드 추가했어요
        var baseResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        baseResolver.setAuthorizationRequestCustomizer(builder ->
                builder.additionalParameters(params -> params.put("prompt", "select_account"))
        );

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger 관련 경로는 무조건 허용
                        .requestMatchers(
                                "/swagger.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/login/**",
                                "/oauth2/**",
                                "/error",
                                "/"
                        ).permitAll()
                        // 2. API 테스트를 위한 경로도 허용
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/materials/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(org.springframework.http.HttpMethod.POST,   "/api/v1/materials/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT,    "/api/v1/materials/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/materials/**").authenticated()
                        // 3. 그 외 기본 페이지 및 리소스 허용
                        .requestMatchers("/", "/login", "/oauth2/**", "/error").permitAll()
                        // 4. 위에서 지정한 경로 외의 모든 경로는 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(ep -> ep.authorizationRequestResolver(baseResolver))
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailureHandler)
                )
                .logout(lo -> lo.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 배포 도메인만 허용 (https)
        cfg.setAllowedOrigins(List.of("https://hufjok.lion.it.kr"));
        // 프론트/스웨거에서 쓰는 메소드/헤더 허용
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
        // 응답에서 노출할 헤더(필요시)
        cfg.setExposedHeaders(List.of("Location"));
        // 세션/쿠키 사용 시 true
        cfg.setAllowCredentials(true);
        // preflight 캐시
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}