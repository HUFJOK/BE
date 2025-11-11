package com.likelion.hufjok.config;

import com.likelion.hufjok.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import java.io.IOException;
import java.util.Locale;

@Slf4j
@Component
public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String ALLOWED_DOMAIN = "hufs.ac.kr";
    private final UserService userService;

    @Value("${frontend.url}")
    private String frontendUrl;


    public CustomOAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException{

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getName();

        String normEmail = (email == null) ? "" : email.toLowerCase(Locale.ROOT);

        if (!normEmail.endsWith("@" + ALLOWED_DOMAIN)) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect("/login?error=unauthorized_domain");
            return;
        }

        userService.findByEmail(normEmail)
                .orElseGet(() -> userService.saveFirstLogin(normEmail, providerId));

        clearAuthenticationAttributes(request);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        log.info("[LOGIN SUCCESS] redirect -> {}", frontendUrl);
        return frontendUrl;
    }

}
