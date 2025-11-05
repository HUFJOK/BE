package com.likelion.hufjok.security.oauth2;

import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.UserRepository;
import com.likelion.hufjok.security.exception.UnauthorizedDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private static final String ALLOWED_DOMAIN = "hufs.ac.kr";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User google = super.loadUser(userRequest);
        Map<String, Object> attrs = google.getAttributes();

        String rawEmail = (String) attrs.get("email");
        if (rawEmail == null) {
            throw new UnauthorizedDomainException("Email scope is missing. Please allow email permission.");
        }
        String email = rawEmail.trim().toLowerCase();

        Object emailVerifiedRaw = attrs.getOrDefault("email_verified", Boolean.TRUE);
        boolean emailVerified = (emailVerifiedRaw instanceof Boolean)
                ? (Boolean) emailVerifiedRaw
                : Boolean.parseBoolean(String.valueOf(emailVerifiedRaw));

        if (!email.endsWith("@" + ALLOWED_DOMAIN)) {
            throw new UnauthorizedDomainException("Access denied: only " + ALLOWED_DOMAIN + " accounts are allowed.");
        }
        if (!emailVerified) {
            throw new UnauthorizedDomainException("Email is not verified by Google.");
        }

        // 구글 고유 id
        String providerId = (String) attrs.getOrDefault("sub", google.getName());

        // 로그인 시점에 저장/업데이트 확정
        saveOrUpdate(email, providerId);

        // ROLE_USER 권한 부여 + nameAttributeKey = "sub"
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attrs,
                "sub"
        );
    }

    @Transactional
    protected void saveOrUpdate(String email, String providerId) {
        userRepository.findByEmail(email)
                .map(u -> {
                    u.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(u);
                })
                .orElseGet(() -> {
                    // ★ 닉네임: 이메일의 @ 앞부분을 기본값으로 사용
                    String defaultNickname;
                    int at = email.indexOf('@');
                    if (at > 0) {
                        defaultNickname = email.substring(0, at);
                    } else {
                        defaultNickname = "user";
                    }

                    return userRepository.save(
                            User.builder()
                                    .email(email)
                                    .socialProvider("Google")
                                    .providerId(providerId)
                                    .major("미입력")
                                    .nickname(defaultNickname)
                                    .build()
                    );
                });
    }
}
