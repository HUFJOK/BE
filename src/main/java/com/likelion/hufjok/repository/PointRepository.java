package com.likelion.hufjok.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.likelion.hufjok.domain.Point;
import com.likelion.hufjok.domain.User;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {
    List<Point> findByUserOrderByCreatedAtDesc(User user);
}
