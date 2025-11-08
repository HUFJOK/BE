package com.likelion.hufjok.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 🚨 여기에 클라이언트(프론트엔드)의 주소를 정확히 입력해야 합니다.
                // 예: "http://localhost:3000", "https://your-frontend-domain.com"
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000")

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

                .allowedHeaders("*")

                // 💡 세션 쿠키(인증 정보)를 주고받기 위해 필수적인 설정입니다.
                .allowCredentials(true)

                .maxAge(3600);
    }
}