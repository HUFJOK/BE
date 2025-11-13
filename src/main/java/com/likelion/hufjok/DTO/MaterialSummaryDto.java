package com.likelion.hufjok.DTO;

// Material 엔티티를 임시로 가정했습니다.
// 실제 domain 패키지에 있는 Material 클래스를 import 해야 할 수 있습니다.
import com.likelion.hufjok.domain.Material;

// 년도 학년 학기 전공
public record MaterialSummaryDto(
        Long id,
        String title,
        Integer year,
        String grade,
        Integer semester,
        String major,
        String courseDivision,
        String professorName,

        Integer reviewCount,
        Integer downloadCount
) {
    public static MaterialSummaryDto from(Material material) {
        int reviewCount = material.getReviews() != null
                ? material.getReviews().size()
                : 0;

        // 다운로드 수 계산
        int downloadCount = material.getDownloads() != null
                ? material.getDownloads().size()
                : 0;
        return new MaterialSummaryDto(
                material.getId(),
                material.getTitle(),
                material.getYear(),
                material.getGrade(),
                material.getSemester(),
                material.getMajor(),
                material.getCourseDivision(),
                material.getProfessorName(),
                reviewCount,
                downloadCount
        );
    }
}