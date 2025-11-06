package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.domain.UserMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserMaterialRepository extends JpaRepository<UserMaterial, Long> {

    // 1. 중복 구매 확인용
    boolean existsByUserAndMaterial(User user, Material material);

    // 2. '내가 다운로드한 목록' 조회용 (N+1 문제 해결을 위해 JOIN FETCH 사용)
    @Query("SELECT um FROM UserMaterial um JOIN FETCH um.material WHERE um.user = :user")
    Page<UserMaterial> findByUserWithMaterial(User user, Pageable pageable);
}
