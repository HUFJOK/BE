package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Material;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MaterialGetResponseDto {

    private Long materialId;
    private String title;
    private String description;
    private String professorName;
    private String courseName;
    private Integer year;
    private Integer semester;
    private String authorName;
    private Long authorId;
    private String grade;
    private String major;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Double avgRating;
    private Integer reviewCount;
    private List<AttachmentDto> attachments;

    public static MaterialGetResponseDto fromEntity(Material material) {
        return MaterialGetResponseDto.builder()
                .materialId(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .professorName(material.getProfessorName())
                .courseName(material.getCourseName())
                .year(material.getYear())
                .semester(material.getSemester())
                .authorName(material.getUser().getNickname())
                .authorId(material.getUser().getId())
                .grade(material.getGrade())
                .major(material.getMajor())
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .reviewCount(material.getReviews() != null ? material.getReviews().size() : 0)
                .attachments(material.getAttachments() != null
                        ? material.getAttachments().stream()
                        .map(attachment -> AttachmentDto.builder()
                                .id(attachment.getId())
                                .originalFileName(attachment.getOriginalFileName())
                                .storedFilePath(attachment.getStoredFilePath())
                                .build())
                        .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
