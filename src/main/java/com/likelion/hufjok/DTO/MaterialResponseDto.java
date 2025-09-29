package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Attachment;
import com.likelion.hufjok.domain.Material;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MaterialResponseDto {

    private final Long id;
    private final String title;
    private final String description;
    private final String authorName;
    private final String courseName;
    private final int year;
    private final int semester;
    private final LocalDateTime createdAt;

    public MaterialResponseDto(Material material) {
        this.id = material.getId();
        this.title = material.getTitle();
        this.description = material.getDescription();
        this.authorName = material.getUser().getNickname(); // User 객체에서 닉네임만 추출
        this.courseName = material.getCourseName();
        this.year = material.getYear();
        this.semester = material.getSemester();
        this.createdAt = material.getCreatedAt();
    }
}
