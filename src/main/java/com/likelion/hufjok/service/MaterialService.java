package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.MaterialRepository;
import com.likelion.hufjok.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final UserRepository userRepository;

    @Transactional
    public MaterialResponseDto createMaterial(Long userId, MaterialCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Material material = Material.builder()
                .title(requestDto.title())
                .description(requestDto.description())
                // TODO: requestDto에서 나머지 필드(교수명, 강의명 등)도 마저 설정해야 합니다.
                .user(user)
                .build();

        Material savedMaterial = materialRepository.save(material);
        return new MaterialResponseDto(savedMaterial);
    }

    public MaterialListResponseDto getMaterials(String keyword, Integer year, Integer semester, String sortBy, int page) {
        Sort sort = sortBy.equalsIgnoreCase("rating")
                ? Sort.by(Sort.Direction.DESC, "avgRating")
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, 10, sort);
        Page<Material> materialsPage;
        if (keyword != null || year != null || semester != null) {
            // 참고: findFilteredMaterials 메소드는 Repository에 직접 구현(JPQL 또는 QueryDSL)해야 합니다.
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
}