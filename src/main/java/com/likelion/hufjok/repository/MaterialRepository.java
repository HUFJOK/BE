package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m " +
            "WHERE (:keyword IS NULL OR m.title LIKE %:keyword% OR m.courseName LIKE %:keyword% OR m.professorName LIKE %:keyword%) " +
            "AND (:year IS NULL OR m.year = :year) " +
            "AND (:semester IS NULL OR m.semester = :semester)")
    Page<Material> findFilteredMaterials(
            @Param("keyword") String keyword,
            @Param("year") Integer year,
            @Param("semester") Integer semester,
            Pageable pageable
    );

}