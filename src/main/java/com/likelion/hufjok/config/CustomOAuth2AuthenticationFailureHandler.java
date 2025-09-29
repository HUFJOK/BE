package com.likelion.hufjok.config;

import com.likelion.hufjok.security.exception.UnauthorizedDomainException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
        throws IOException, ServletException {

        String redirect = "/login?error=" +
                (exception instanceof UnauthorizedDomainException ? "unauthorized_domain" : "oauth_error");
        getRedirectStrategy().sendRedirect(request, response, redirect);
    }
}
