package com.likelion.hufjok.DTO;

// Material 엔티티를 임시로 가정했습니다.
// 실제 domain 패키지에 있는 Material 클래스를 import 해야 할 수 있습니다.
import com.likelion.hufjok.domain.Material;

public record MaterialSummaryDto(
        Long id,
        String title,
        String professorName
        // ... 필요한 필드가 더 있다면 추가
) {
    // 이 부분을 추가해주세요!
    // Material 객체를 받아서 MaterialSummaryDto 객체로 변환해주는 메소드입니다.
    public static MaterialSummaryDto from(Material material) {
        return new MaterialSummaryDto(
                material.getId(),
                material.getTitle(),
                material.getProfessorName()
                // ... 필드에 맞게 추가
        );
    }
}