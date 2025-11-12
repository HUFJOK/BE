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
        String courseDivision,
        String professorName
) {
    public static MaterialSummaryDto from(Material material) {
        return new MaterialSummaryDto(
                material.getId(),
                material.getTitle(),
                material.getYear(),
                material.getGrade(),
                material.getSemester(),
                material.getCourseDivision(),
                material.getProfessorName()
        );
    }
}