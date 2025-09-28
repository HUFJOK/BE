package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*; // DTO들을 모두 사용하기 위해 import
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.repository.MaterialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    // UserRepository 등 다른 Repository도 필요에 따라 주입받습니다.

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    // 기존 자료 조회 메소드 (수정 없음)
    public MaterialListResponseDto getMaterials(String keyword, Integer year, Integer semester, String sortBy, int page) {
        Sort sort = sortBy.equalsIgnoreCase("rating")
                ? Sort.by(Sort.Direction.DESC, "avgRating")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, 10, sort);
        Page<Material> materialsPage;
        if (keyword != null || year != null || semester != null) {
            materialsPage = materialRepository.findFilteredMaterials(keyword, year, semester, pageable);
        } else {
            materialsPage = materialRepository.findAll(pageable);
        }
        List<MaterialSummaryDto> materialDtos = materialsPage.getContent().stream()
                .map(MaterialSummaryDto::from)
                .collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo(
                materialsPage.getNumber() + 1,
                materialsPage.getTotalPages(),
                materialsPage.getTotalElements()
        );
        return new MaterialListResponseDto(pageInfo, materialDtos);
    }

    // 기존 자료 수정 메소드 (수정 없음)
    @Transactional
    public MaterialUpdateResponseDto updateMaterial(Long materialId, Long userId, MaterialUpdateRequestDto request) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));
        if (!material.getUser().getId().equals(userId)) {
            throw new RuntimeException("수정할 권한이 없습니다.");
        }
        material.setTitle(request.title());
        material.setDescription(request.description());
        return MaterialUpdateResponseDto.from(material);
    }

    // --- 이 부분이 새로 추가되었습니다 ---
    @Transactional // 데이터를 변경하는 작업이므로 @Transactional을 붙여줍니다.
    public void deleteMaterial(Long materialId, Long userId) {
        // 1. DB에서 삭제할 자료를 찾아옵니다.
        // 자료가 없으면 NotFoundException 발생 (404 Not Found 응답)
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        // 2. 권한 확인 (매우 중요!)
        // 자료를 올린 사람(material.getUser().getId())과
        // 현재 요청한 사람(userId)이 동일한지 확인합니다.
        if (!material.getUser().getId().equals(userId)) {
            // 동일하지 않으면 권한 없음 예외 발생 (403 Forbidden 응답)
            throw new RuntimeException("삭제할 권한이 없습니다.");
        }

        // 3. 권한이 있으면, Repository를 통해 해당 자료를 DB에서 삭제합니다.
        materialRepository.delete(material);
    }
}