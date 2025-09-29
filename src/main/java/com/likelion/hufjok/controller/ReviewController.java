package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.ReviewGetResponseDto;
import com.likelion.hufjok.DTO.ReviewUpdateRequestDto;
import com.likelion.hufjok.DTO.ReviewUpdateRequestDto;
import com.likelion.hufjok.security.UserDetailsImpl;
import com.likelion.hufjok.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. 자료 후기 조회 API
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewGetResponseDto> getReview(@PathVariable Long reviewId) {
        ReviewGetResponseDto responseDto = reviewService.findById(reviewId);
        return ResponseEntity.ok(responseDto);
    }

    // 2. 자료 후기 수정 API
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ReviewUpdateRequestDto requestDto) throws AccessDeniedException {

        reviewService.update(reviewId, userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok().build();
    }

    // 3. 자료 후기 삭제 API
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {

        reviewService.delete(reviewId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}