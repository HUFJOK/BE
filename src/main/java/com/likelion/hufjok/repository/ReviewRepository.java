package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMaterialIdOrderByCreatedAtDesc(Long materialId);
}