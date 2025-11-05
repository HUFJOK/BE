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

    private Integer earnedPoints;
    private Integer currentPoints;
    private String pointMessage;

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
                .earnedPoints(200)
                .currentPoints(material.getUser().getPoints())
                .pointMessage("자료 게시로 200P가 적립되었습니다!")
                .build();
    }
}