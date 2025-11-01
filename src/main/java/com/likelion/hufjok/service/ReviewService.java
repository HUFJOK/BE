package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.ReviewCreateRequestDto;
import com.likelion.hufjok.DTO.ReviewCreateResponseDto;
import com.likelion.hufjok.DTO.ReviewGetResponseDto;
import com.likelion.hufjok.DTO.ReviewUpdateRequestDto;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.Review;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.MaterialRepository;
import com.likelion.hufjok.repository.ReviewRepository;
import com.likelion.hufjok.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, MaterialRepository materialRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
    }

    public ReviewCreateResponseDto createReview(Long materialId, Long userId, ReviewCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(user)
                .material(material)
                .build();

        Review savedReview = reviewRepository.save(review);

        return ReviewCreateResponseDto.from(savedReview);
    }

    public ReviewGetResponseDto findById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));
        return new ReviewGetResponseDto(review);
    }

    @Transactional
    public void update(Long reviewId, Long currentUserId, ReviewUpdateRequestDto dto) throws AccessDeniedException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));

        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("후기를 수정할 권한이 없습니다.");
        }
    }

    @Transactional
    public void delete(Long reviewId, Long currentUserId) throws AccessDeniedException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));

        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("후기를 삭제할 권한이 없습니다.");
        }
        reviewRepository.delete(review);
    }
}