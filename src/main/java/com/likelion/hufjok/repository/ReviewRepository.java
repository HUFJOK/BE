package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}