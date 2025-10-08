package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*; // DTO들을 모두 사용하기 위해 import
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.repository.MaterialRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

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

    @Transactional
    public void deleteMaterial(Long materialId, Long userId) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        if (!material.getUser().getId().equals(userId)) {

            throw new RuntimeException("삭제할 권한이 없습니다.");
        }

        materialRepository.delete(material);
    }

    @Transactional
    public MaterialCreateResponseDto createMaterial(Long userId,
                                                    @Valid MaterialCreateRequestDto metadata,
                                                    MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file.required");
        }

        String filePath = "/uploads/" + file.getOriginalFilename();

        Material material = Material.builder()
                .title(metadata.getTitle())
                .description(metadata.getDescription())
                .professorName(metadata.getProfessorName())
                .courseName(metadata.getCourseName())
                .year(metadata.getYear())
                .semester(metadata.getSemester())
                .filePath(filePath)
                .build();
        Material saved = materialRepository.save(material);
        return MaterialCreateResponseDto.fromEntity(saved);
    }
}