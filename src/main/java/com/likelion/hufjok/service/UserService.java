package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.OnboardingRequestDto;
import com.likelion.hufjok.DTO.OnboardingResponseDto;
import com.likelion.hufjok.DTO.UserUpdateRequestDto;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PointService pointService;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User saveFirstLogin(String email, String providerId) {
        String normEmail = email == null ? "" : email.toLowerCase();

        // 기본 닉네임: 이메일 @ 앞부분 (없으면 "user")
        String nickname = "user";
        int at = normEmail.indexOf('@');
        if (at > 0) {
            nickname = normEmail.substring(0, at);
        }

        User u = User.builder()
                .email(normEmail)
                .socialProvider("Google")
                .providerId(providerId)
                .major("미입력")
                .nickname(nickname)  // ★ NOT NULL 컬럼 채움
                .points(0)           // ★ NOT NULL이면 기본값
                .build();

        User saved = userRepository.save(u);

        return saved;
    }

    @Transactional
    public User updateMyInfo(String email, UserUpdateRequestDto req) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", email));


        if (req.getNickname() != null && !req.getNickname().isBlank()) {
            user.setNickname(req.getNickname());
        }
        if (req.getMajor() != null && !req.getMajor().isBlank()) {
            user.setMajor(req.getMajor());
        }

        if (req.getDoubleMajor() != null) user.setDoubleMajor(req.getDoubleMajor());
        if (req.getMinor() != null)      user.setMinor(req.getMinor());

        return userRepository.save(user);
    }

    // 전공, 부전공, 이중전공 입력
    @Transactional
    public OnboardingResponseDto createMajor(String email, OnboardingRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", email));

        if (dto.getMajor() != null && !dto.getMajor().isBlank()) {
            user.setMajor(dto.getMajor());
        }

        if (dto.getMinor() != null && !dto.getMinor().isBlank()) {
            user.setMinor(dto.getMinor());
        }

        User savedUser = userRepository.save(user);

        return OnboardingResponseDto.from(savedUser);
    }

    @Transactional
    public OnboardingResponseDto createDoubleMajor(String email, OnboardingRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", email));

        if (dto.getMajor() != null && !dto.getMajor().isBlank()) {
            user.setMajor(dto.getMajor());
        }

        User savedUser = userRepository.save(user);

        return OnboardingResponseDto.from(savedUser);
    }


    // 이중/부전 삭제
    @Transactional
    public User clearMinor(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", email));
        user.setMinor(null);
        return userRepository.save(user);
    }

}
