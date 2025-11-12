package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Material;
import java.time.LocalDateTime;

public record MaterialUpdateResponseDto(
        Long id,
        String title,
        String description,
        String professorName,
        String courseName,
        Integer year,
        Integer semester,
        String major,
        String courseDivision,
        String grade,
        LocalDateTime updatedAt
) {
    public static MaterialUpdateResponseDto from(Material material) {
        return new MaterialUpdateResponseDto(
                material.getId(),
                material.getTitle(),
                material.getDescription(),
                material.getProfessorName(),
                material.getCourseName(),
                material.getYear(),
                material.getSemester(),
                material.getMajor(),
                material.getCourseDivision(),
                material.getGrade(),
                material.getUpdatedAt()
        );
    }
}