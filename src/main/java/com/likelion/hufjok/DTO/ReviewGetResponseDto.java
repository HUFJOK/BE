package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Review;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewGetResponseDto {
    private final Long reviewId;
    private final String authorNickname;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;

    public ReviewGetResponseDto(Review review) {
        this.reviewId = review.getId();
        this.authorNickname = review.getUser().getNickname();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
    }
}