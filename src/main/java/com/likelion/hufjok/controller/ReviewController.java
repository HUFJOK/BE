package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.ReviewCreateRequestDto;
import com.likelion.hufjok.DTO.ReviewCreateResponseDto;
import com.likelion.hufjok.DTO.ReviewGetResponseDto;
import com.likelion.hufjok.DTO.ReviewUpdateRequestDto;
import com.likelion.hufjok.security.UserDetailsImpl;
import com.likelion.hufjok.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewGetResponseDto> getReview(@PathVariable Long reviewId) {
        ReviewGetResponseDto responseDto = reviewService.findById(reviewId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ReviewUpdateRequestDto requestDto) throws AccessDeniedException {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.update(reviewId, userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        reviewService.delete(reviewId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews")
    public ResponseEntity<ReviewCreateResponseDto> createReview(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody @Valid ReviewCreateRequestDto requestDto) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUser().getId();
        Long materialId = requestDto.getMaterialId();

        ReviewCreateResponseDto responseDto = reviewService.createReview(
                materialId,
                userId,
                requestDto
        );
        return ResponseEntity.ok(responseDto);
    }
}