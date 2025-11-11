package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.PointResponseDto;
import com.likelion.hufjok.domain.Point;
import com.likelion.hufjok.domain.PointHistory;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.PointHistoryRepository;
import com.likelion.hufjok.repository.PointRepository;
import com.likelion.hufjok.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private static final int SIGNUP_BONUS_AMOUNT = 500;

    // 현재 내 포인트 조회
    @Transactional(readOnly = true)
    public int getUserPoints(String email) {
        String norm = email == null ? "" : email.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
        return user.getPoints();
    }

    //회원가입 시 포인트 500 준거 히스토리에 기록
    public void awardSignupBonus(String email) {
        String norm = email == null ? "" : email.toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

        if (user.isBonusAwarded()) return ;

        updatePoints(norm, SIGNUP_BONUS_AMOUNT, "회원가입 보상", PointHistory.PointType.SIGNUP_BONUS);

        user.setBonusAwarded(true);
        userRepository.save(user);
    }


    // 포인트 전체 이력 조회
    @Transactional(readOnly = true)
    public List<PointResponseDto> getPointHistory(String email) {
        String norm = email == null ? "" : email.toLowerCase();

        User user = userRepository.findByEmail(norm)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

        List<PointHistory> histories = pointHistoryRepository.findByUserOrderByCreatedAtDesc(user);

        // .stream(): 리스트를 순차적으로 하나씩 꺼내서 가공할 수 있게 해주는 자바함수형 API
        return histories.stream()
                .map(history -> PointResponseDto.builder()
                        .amount(history.getAmountChange())
                        .reason(history.getReason() != null ? history.getReason() : history.getType().name())
                        .createdAt(history.getCreatedAt())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }


    // 포인트 적립, 차감 등 (업데이트)
    public void updatePoints(String email, int amount, String reason, PointHistory.PointType type) {
        String norm = email == null ? "" : email.toLowerCase();

        User user = userRepository.findByEmail(norm)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));

        int delta = 0;

        if (type == PointHistory.PointType.ADJUST) {
            if (amount == 0) throw new IllegalArgumentException("조정 금액은 0일 수 없습니다.");
            delta = amount;
        } else {
            if (amount <= 0) throw new IllegalArgumentException("amount는 양수여야 합니다.");
            delta = type.signedAmount(amount);
        }

        // 포인트가 부족하면 패스
        int newBalance = user.getPoints() + delta;

        if (type == PointHistory.PointType.USE && newBalance < 0) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다. 현재 잔액: " + user.getPoints());
        }

        user.setPoints(newBalance);

        // Point 테이블 기록
        Point point = Point.builder()
                .amount(delta)
                .reason(reason)
                .user(user)
                .build();
        pointRepository.save(point);

        // PointHistory 테이블 기록
        PointHistory history = PointHistory.builder()
                .user(user)
                .amountChange(delta)
                .amountAfter(newBalance)
                .reason(reason)
                .type(type)
                .build();
        pointHistoryRepository.save(history);
    }
}
