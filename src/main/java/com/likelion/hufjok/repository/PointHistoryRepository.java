package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Point;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.domain.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    List<PointHistory> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndType(User user, PointHistory.PointType type);
}
