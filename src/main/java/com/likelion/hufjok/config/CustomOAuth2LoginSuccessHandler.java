package com.likelion.hufjok.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String ALLOWED_DOMAIN = "hufs.ac.kr";
    private static final String TARGET_AFTER_LOGIN = "/swagger-ui/index.html";

    public CustomOAuth2LoginSuccessHandler() {
        setAlwaysUseDefaultTargetUrl(true);
        setDefaultTargetUrl(TARGET_AFTER_LOGIN);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email != null && email.endsWith("@" + ALLOWED_DOMAIN)) {
            // 같은 오리진의 스웨거로 확실히 리다이렉트
            clearAuthenticationAttributes(request); // 남은 속성 정리
            getRedirectStrategy().sendRedirect(request, response, TARGET_AFTER_LOGIN);
        } else {
            // 허용 도메인 아니면 세션 제거 후 로그인 페이지로
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect("/login?error=unauthorized_domain");
        }
    }
}
