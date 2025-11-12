package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Review;
import java.time.LocalDateTime;

public record ReviewCreateResponseDto(
        Long reviewId,
        String authorNickname,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewCreateResponseDto from(Review review) {
        return new ReviewCreateResponseDto(
                review.getId(),
                review.getUser().getNickname(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
