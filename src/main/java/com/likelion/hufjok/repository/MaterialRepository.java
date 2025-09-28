package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    // 검색/필터 로직을 위한 동적 쿼리 예시
    @Query("SELECT m FROM Material m " +
            "WHERE (:keyword IS NULL OR m.title LIKE %:keyword% OR m.courseName LIKE %:keyword% OR m.professorName LIKE %:keyword%) " +
            "AND (:year IS NULL OR m.year = :year) " +
            "AND (:semester IS NULL OR m.semester = :semester)")
    Page<Material> findFilteredMaterials(
            @Param("keyword") String keyword,
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            Pageable pageable // 여기에 정렬(sortBy)과 페이징(page) 정보가 모두 담깁니다.
    );

    // 개인화 로직은 ORDER BY CASE 구문을 사용하는 등 더 복잡한 쿼리가 필요합니다.
    // 예: @Query("SELECT m FROM Material m ORDER BY CASE WHEN m.courseName = :major THEN 1 ...")
}