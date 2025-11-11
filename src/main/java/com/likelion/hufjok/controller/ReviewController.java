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
import java.util.List;

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
    public ResponseEntity<ReviewGetResponseDto> getReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long currentUserId = null;
        if (principal != null) {
            String email = principal.getAttribute("email");
            if (email != null) {
                // UserService를 통해 ID 획득 (UserService는 이미 Controller에 주입되어 있다고 가정)
                currentUserId = userService.findByEmail(email.toLowerCase())
                        .map(User::getId)
                        .orElse(null);
            }
        }

        ReviewGetResponseDto responseDto = reviewService.findById(reviewId, currentUserId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/materials/{materialId}/reviews")
    @Operation(
            summary = "자료별 리뷰 전체 조회",
            description = "특정 자료 ID에 속한 모든 리뷰를 조회합니다. 응답에 작성자 여부(isAuthor) 포함.",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<List<ReviewGetResponseDto>> getReviewsByMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal OAuth2User principal) {

        Long currentUserId = null; // 기본값은 null (로그인하지 않은 사용자 또는 사용자 정보 없음)

        if (principal != null) {
            String email = principal.getAttribute("email");

            // 💡 [수정] 이메일이 있을 경우에만, Optional 체인을 통해 ID를 가져오고, 없으면 null을 반환
            if (email != null) {
                currentUserId = userService.findByEmail(email.toLowerCase())
                        .map(User::getId)
                        .orElse(null);
            }
        }

        List<ReviewGetResponseDto> responseList = reviewService.getReviewsByMaterialId(materialId, currentUserId);

        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(
            summary = "리뷰 수정",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal OAuth2User principal,
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