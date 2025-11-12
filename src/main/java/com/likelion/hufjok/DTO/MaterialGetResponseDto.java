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
    private Integer downloadCount;
    private List<AttachmentDto> attachments;

    public static MaterialGetResponseDto fromEntity(Material material) {
        // 리뷰 개수 계산
        int reviewCount = material.getReviews() != null ? material.getReviews().size() : 0;
        
        // 다운로드 수 계산
        int downloadCount = material.getDownloads() != null ? material.getDownloads().size() : 0;
        
        // 평균 평점 계산
        Double avgRating = null;
        if (material.getReviews() != null && !material.getReviews().isEmpty()) {
            avgRating = material.getReviews().stream()
                    .mapToInt(review -> review.getRating())
                    .average()
                    .orElse(0.0);
            // 소수점 두 자리까지 반올림
            avgRating = Math.round(avgRating * 100.0) / 100.0;
        }
        
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
                .avgRating(avgRating)
                .reviewCount(reviewCount)
                .downloadCount(downloadCount)
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
