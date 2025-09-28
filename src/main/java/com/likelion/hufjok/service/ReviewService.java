package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.ReviewCreateRequestDto;
import com.likelion.hufjok.DTO.ReviewCreateResponseDto;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.Review;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.MaterialRepository;
import com.likelion.hufjok.repository.ReviewRepository;
import com.likelion.hufjok.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}