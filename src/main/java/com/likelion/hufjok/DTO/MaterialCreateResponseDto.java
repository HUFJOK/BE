package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Material;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MaterialCreateResponseDto {

    private Long materialId;
    private String title;
    private String description;
    private String professorName;
    private String courseName;
    private int year;
    private int semester;
    private LocalDateTime createdAt;
    private String courseDivision;

    public static MaterialCreateResponseDto fromEntity(Material material) {
        return MaterialCreateResponseDto.builder()
                .materialId(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .professorName(material.getProfessorName())
                .courseName(material.getCourseName())
                .year(material.getYear())
                .semester(material.getSemester())
                .createdAt(material.getCreatedAt())
                .build();
    }
}