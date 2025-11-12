package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Review;
import java.time.LocalDateTime;

public record ReviewCreateResponseDto(
        Long reviewId,
        String authorNickname,
        Integer rating,
        String comment,
        int reviewCount,
        LocalDateTime createdAt
) {
    public static ReviewCreateResponseDto from(Review review) {
        return new ReviewCreateResponseDto(
                review.getId(),
                review.getUser().getNickname(), // User 엔티티에서 닉네임 가져오기
                review.getRating(),
                review.getComment(),
                review.getReviewCount(),
                review.getCreatedAt()
        );
    }
}