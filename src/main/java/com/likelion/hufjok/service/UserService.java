package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.OnboardingRequestDto;
import com.likelion.hufjok.DTO.OnboardingResponseDto;
import com.likelion.hufjok.DTO.UserUpdateRequestDto;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
                .points(500)  // 회원가입 보너스 포인트 직접 설정
                .bonusAwarded(false)  // 아직 히스토리에 기록되지 않음
                .build();

        User saved = userRepository.saveAndFlush(u);
        
        // 포인트 히스토리에 회원가입 보너스 기록
        pointService.awardSignupBonus(saved.getEmail());
        
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

        if (req.getMinor() != null)      user.setMinor(req.getMinor());

        return userRepository.save(user);
    }

    // 전공, 2전공 입력
    @Transactional
    public OnboardingResponseDto createMajor(String email, OnboardingRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.", email));

        if (user.getIsOnboardingCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 온보딩을 완료한 사용자입니다.");
        }

        if (dto.getMajor() != null && !dto.getMajor().isBlank()) {
            user.setMajor(dto.getMajor());
        }

        if (dto.getMinor() != null && !dto.getMinor().isBlank()) {
            user.setMinor(dto.getMinor());
        }

        user.setIsOnboardingCompleted(true);

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
