package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.ReviewCreateRequestDto;
import com.likelion.hufjok.DTO.ReviewCreateResponseDto;
import com.likelion.hufjok.DTO.ReviewGetResponseDto;
import com.likelion.hufjok.DTO.ReviewUpdateRequestDto;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.service.ReviewService;
import com.likelion.hufjok.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;

    @GetMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 단건 조회",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<ReviewGetResponseDto> getReview(@PathVariable Long reviewId) {
        ReviewGetResponseDto responseDto = reviewService.findById(reviewId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal OAuth2User principal, // ⭐ principal로 받음
            @RequestBody @Valid ReviewUpdateRequestDto requestDto) throws AccessDeniedException {

        // ⭐ principal null 체크
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        // ⭐ principal에서 이메일 추출 → userId 얻기
        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        reviewService.update(reviewId, userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 삭제",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal OAuth2User principal) throws AccessDeniedException {

        // principal null 체크
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        // principal에서 이메일 추출 → userId 얻기
        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        reviewService.delete(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews")
    @Operation(
            summary = "리뷰 작성",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<ReviewCreateResponseDto> createReview(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody @Valid ReviewCreateRequestDto requestDto) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        // principal에서 이메일 추출 → userId 얻기
        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        Long materialId = requestDto.getMaterialId();


        try {
            ReviewCreateResponseDto responseDto = reviewService.createReview(
                    materialId,
                    userId,
                    requestDto
            );
            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            e.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "리뷰 처리 중 예외 발생: " + e.getMessage(), e
            );
        }
    }
}