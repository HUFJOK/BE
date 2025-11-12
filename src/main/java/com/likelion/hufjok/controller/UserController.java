package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.PointHistory;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.security.oauth2.UserPrincipal;
import com.likelion.hufjok.service.PointService;
import com.likelion.hufjok.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final PointService pointService;

    @PostMapping("/onboarding")
    @Operation(
            summary = "전공 정보 입력",
            description = "온보딩에서 전공/이중전공 또는 부전공을 입력합니다." ,
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<OnboardingResponseDto> getMajor(@AuthenticationPrincipal OAuth2User oAuth2User,
                                                         @RequestBody @Valid OnboardingRequestDto requestDto) {

        if (oAuth2User == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        OnboardingResponseDto responseDto = userService.createMajor(email, requestDto);

        return ResponseEntity.ok(responseDto);
    }


    @GetMapping("/mypage/me")
    @Operation(
            summary = "기본 정보 조회",
            description = "로그인한 사용자의 기본 정보를 조회합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userService.findByEmail(email)
                .orElseGet(()-> userService.saveFirstLogin(email, principal.getName()));

        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @GetMapping("/mypage/points/amount")
    @Operation(
            summary = "내 포인트 잔액",
            description = "현재 내 포인트의 잔액을 확인합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication"))
    public ResponseEntity<?> getMyPointAmount(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String email = principal.getAttribute("email");
        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        int amount = pointService.getUserPoints(email);
        return ResponseEntity.ok(new PointResponseDto(amount, "잔액 조회", LocalDateTime.now(), email));
    }

    @GetMapping("/mypage/points/history")
    @Operation(
            summary = "내 포인트 이력",
            description = "내 포인트의 이력을 확인합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication"))
    public ResponseEntity<?> getMyPointHistory(@AuthenticationPrincipal OAuth2User principal) {
        System.out.println("=== /api/v1/users/mypage/points/history 호출됨 ===");
        
        if (principal == null) {
            System.out.println("principal이 null입니다!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getAttribute("email");
        System.out.println("email: " + email);
        
        if (email == null) {
            System.out.println("email이 null입니다!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        List<PointResponseDto> history = pointService.getPointHistory(email);
        System.out.println("조회된 포인트 이력 개수: " + history.size());
        return ResponseEntity.ok(history);
    }


    @PostMapping("/mypage/points/earn")
    @Operation(
            summary = "포인트 적립",
            description = "요청한 amount만큼 포인트를 적립합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<?> earnPoints(@AuthenticationPrincipal OAuth2User principal,
                                        @Valid @RequestBody PointChangeRequest req) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = principal.getAttribute("email");

        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            pointService.updatePoints(email, req.getAmount(), req.getReason(), PointHistory.PointType.EARN);
            int balance = pointService.getUserPoints(email);
            return ResponseEntity.ok(new PointResponseDto(balance, "족보 업로드", LocalDateTime.now(), email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/mypage/points/use")
    @Operation(
            summary = "포인트 사용",
            description = "요청한 amount만큼 포인트를 사용합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<?> usePoints(@AuthenticationPrincipal OAuth2User principal,
                                        @Valid @RequestBody PointChangeRequest req) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = principal.getAttribute("email");

        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        try {
            pointService.updatePoints(email, req.getAmount(), req.getReason(), PointHistory.PointType.USE);
            int balance = pointService.getUserPoints(email);
            return ResponseEntity.ok(new PointResponseDto(balance, "족보 구매", LocalDateTime.now(), email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // 기본 정보 변경
    @PutMapping("/mypage/me")
    @Operation(
            summary = "기본 정보 변경",
            description = "닉네임/전공/이중전공 또는 부전공을 전달된 값으로 전체 교체합니다.",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<UserResponseDto> updateMyInfo(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String email = principal.getAttribute("email");
        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        System.out.println("[PUT] dto=" + request);

        User updated = userService.updateMyInfo(email, request);
        return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
    }



//    @DeleteMapping("mypage/me")
//    @Operation(summary = "이중전공/부전공 삭제",
//            description = "majorType에 해당하는 전공 정보 삭제합니다. (majorType: doubleMajor 또는 minor)",
//            security = @SecurityRequirement(name = "Cookie Authentication"))
//    public ResponseEntity<UserResponseDto> deleteMajor(@AuthenticationPrincipal OAuth2User principal) {
//        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        String email = principal.getAttribute("email");
//        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//
//        User updated = null;
//
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(UserResponseDto.builder()
//                        .nickname(null)
//                        .major(null)
//                        .minor(null)
//                        .email(null)
//                        .isOnboarding(false)
//                        .build()
//                );
//        }
//        return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
//    }

}

