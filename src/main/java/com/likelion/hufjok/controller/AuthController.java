package com.likelion.hufjok.controller;

import com.likelion.hufjok.security.oauth2.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {
    @GetMapping({"/", "/api/v1"})
    @ResponseBody
    public String home(@AuthenticationPrincipal OAuth2User userPrincipal) {
        if (userPrincipal != null) {
            String email = userPrincipal.getAttribute("email");
            return "환영합니다, " + email + "님! HUFS 인증에 성공하셨습니다.";
        } else {
            return "HUFS 자료 공유 서비스에 오신 것을 환영합니다. <a href='/oauth2/authorization/google'>Google로 로그인</a>하세요.";
        }
    }
}
