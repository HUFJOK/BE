package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Review;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ReviewGetResponseDto {
    private final Long reviewId;
    private final String authorNickname;
    private final String reviewerEmail;
    private final int rating;
    private final String comment;
    private final LocalDateTime createdAt;
    private final boolean isAuthor;

    public ReviewGetResponseDto(Review review, boolean isAuthor) {
        this.reviewId = review.getId();
        this.authorNickname = review.getUser().getNickname();
        this.reviewerEmail = review.getUser().getEmail();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.createdAt = review.getCreatedAt();
        this.isAuthor = isAuthor();
    }
}