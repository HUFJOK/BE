package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Attachment;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.PointHistory;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.repository.AttachmentRepository;
import com.likelion.hufjok.repository.MaterialRepository;
import com.likelion.hufjok.repository.UserRepository;
import com.likelion.hufjok.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final UserRepository userRepository;
    private final PointService pointService;

    @Transactional
    public MaterialCreateResponseDto createMaterial(Long userId,
                                                    @Valid MaterialCreateRequestDto metadata,
                                                    List<MultipartFile> files) throws IOException {

        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new IllegalArgumentException("file.required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Material material = Material.builder()
                .title(metadata.getTitle())
                .description(metadata.getDescription())
                .professorName(metadata.getProfessorName())
                .courseName(metadata.getCourseName())
                .year(metadata.getYear())
                .semester(metadata.getSemester())
                .user(user)
                .build();

        Material savedMaterial = materialRepository.save(material);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                AttachmentDto attachmentInfo = attachmentService.saveFileAndGetInfo(file);

                Attachment attachment = Attachment.builder()
                        .originalFileName(attachmentInfo.getOriginalFileName())
                        .storedFilePath(attachmentInfo.getStoredFilePath())
                        .build();

                attachment.setMaterial(savedMaterial);
                attachmentRepository.save(attachment);
            }
        }

        pointService.updatePoints(
                user.getEmail(),
                200,
                "자료 게시: " + savedMaterial.getTitle(),
                PointHistory.PointType.EARN
        );

        return MaterialCreateResponseDto.fromEntity(savedMaterial);
    }

    public MaterialGetResponseDto getMaterial(Long materialId) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        return MaterialGetResponseDto.fromEntity(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId, Long userId) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        if (!material.getUser().getId().equals(userId)) {
            throw new RuntimeException("삭제할 권한이 없습니다.");
        }

        for (Attachment attachment : material.getAttachments()) {
            try {
                attachmentService.deleteFileByPath(attachment.getStoredFilePath());
                attachmentRepository.delete(attachment);
            } catch (IOException e) {
                System.err.println("파일 삭제 실패: " + attachment.getStoredFilePath() + " | 오류: " + e.getMessage());
            }
        }

        materialRepository.delete(material);
    }

    public MaterialListResponseDto getMaterials(String keyword, Integer year, Integer semester, String sortBy, int page) {
// 임시 수정안
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
// if (sortBy.equalsIgnoreCase("rating")) {
//     sort = Sort.by(Sort.Direction.DESC, "avgRating");
// }
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

    public MaterialListResponseDto getMyUploadedMaterials(Long userId, int page) {
        // 1. 정렬 기준 (최신순)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, 10, sort);

        // 2. Repository 호출 (새로 만든 메소드 사용)
        Page<Material> materialsPage = materialRepository.findByUserId(userId, pageable);

        // 3. DTO로 변환
        List<MaterialSummaryDto> materialDtos = materialsPage.getContent().stream()
                .map(MaterialSummaryDto::from)
                .collect(Collectors.toList());

        // 4. 페이지 정보 DTO 생성
        PageInfo pageInfo = new PageInfo(
                materialsPage.getNumber() + 1,
                materialsPage.getTotalPages(),
                materialsPage.getTotalElements()
        );

        // 5. 최종 응답 DTO로 반환
        return new MaterialListResponseDto(pageInfo, materialDtos);
    }
}
