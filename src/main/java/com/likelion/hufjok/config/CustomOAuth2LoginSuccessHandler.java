package com.likelion.hufjok.config;

import com.likelion.hufjok.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;

@Component
public class CustomOAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String ALLOWED_DOMAIN = "hufs.ac.kr";
    private final UserService userService;


    public CustomOAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        setAlwaysUseDefaultTargetUrl(false);
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getName();

        String normEmail = (email == null) ? "" : email.toLowerCase(Locale.ROOT);

        if (normEmail.endsWith("@" + ALLOWED_DOMAIN)) {
            userService.findByEmail(normEmail)
                    .orElseGet(() -> userService.saveFirstLogin(normEmail, providerId));

            clearAuthenticationAttributes(request);

            String target = resolveFrontendTarget(request);
            getRedirectStrategy().sendRedirect(request, response, target);
        } else {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect("/login?error=unauthorized_domain");
        }
    }

    private String resolveFrontendTarget(HttpServletRequest request) {
        String host = request.getServerName();
        if ("hufjok.lion.it.kr".equalsIgnoreCase(host)) {
            return "https://hufjok.lion.it.kr/loading";
        }
        return "http://localhost:5173/loading";
    }
}
