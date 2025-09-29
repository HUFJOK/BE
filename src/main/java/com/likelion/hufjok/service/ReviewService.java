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
        // 1. 후기를 작성할 유저와 대상 자료를 DB에서 찾아옵니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        // 2. 새로운 Review 엔티티를 생성합니다.
        Review review = Review.builder()
                .rating(request.rating())
                .comment(request.comment())
                .user(user)
                .material(material)
                .build();

        // 3. 생성한 Review를 DB에 저장합니다.
        Review savedReview = reviewRepository.save(review);

        // 4. 저장된 Review를 DTO로 변환하여 Controller에 반환합니다.
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
        review.updateReview(dto.rating(), dto.comment());
    }

    @Transactional
    public void delete(Long reviewId, Long currentUserId) throws AccessDeniedException {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("해당 후기를 찾을 수 없습니다. ID: " + reviewId));

        // ⭐ 권한 검증: 작성자와 현재 유저가 동일한지 확인
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("후기를 삭제할 권한이 없습니다.");
        }
        reviewRepository.delete(review);
    }


}