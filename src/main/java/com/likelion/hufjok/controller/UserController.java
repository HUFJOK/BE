package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.OnboardingRequestDto;
import com.likelion.hufjok.DTO.OnboardingResponseDto;
import com.likelion.hufjok.DTO.UserResponseDto;
import com.likelion.hufjok.DTO.UserUpdateRequestDto;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.security.oauth2.UserPrincipal;
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

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("/onboarding")
    @Operation(
            summary = "전공 정보 입력",
            description = "온보딩에서 전공/이중전공/부전공을 입력합니다." ,
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<OnboardingResponseDto> getMajor(@AuthenticationPrincipal OAuth2User oAuth2User,
                                                         @RequestBody @Valid OnboardingRequestDto requestDto) {

        String email = oAuth2User.getAttribute("email");

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

    // 기본 정보 변경
    @PutMapping("/mypage/me")
    @Operation(
            summary = "기본 정보 변경",
            description = "닉네임/전공/복수전공/부전공을 전달된 값으로 전체 교체합니다.",
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



    @DeleteMapping("mypage/me/doubleMajor")
    @Operation(summary = "이중전공 삭제", security = @SecurityRequirement(name = "Cookie Authentication"))
    public ResponseEntity<UserResponseDto> deleteDoubleMajor(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = principal.getAttribute("email");
        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User updated = userService.clearDoubleMajor(email);
        return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
    }

    @DeleteMapping("mypage/me/minor")
    @Operation(summary = "부전공 삭제", security = @SecurityRequirement(name = "Cookie Authentication"))
    public ResponseEntity<UserResponseDto> deleteMinor(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String email = principal.getAttribute("email");
        if (email == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        User updated = userService.clearMinor(email);
        return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
    }

}

