package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.*; // User, Material, Attachment, PointHistory, UserMaterial
import com.likelion.hufjok.repository.*; // 5개 Repository
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 생성자 자동 주입
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final AttachmentRepository attachmentRepository;
    private final AttachmentService attachmentService;
    private final UserRepository userRepository;
    private final PointService pointService;

    // ▼▼▼ 1. '구매 내역' 저장을 위해 추가 ▼▼▼
    private final UserMaterialRepository userMaterialRepository;

    @Transactional
    public MaterialCreateResponseDto createMaterial(Long userId,
                                                    @Valid MaterialCreateRequestDto metadata,
                                                    List<MultipartFile> files) throws IOException {
        
        // (이하 `createMaterial` 메소드는 '옥민희'님이 구현한 `develop` 브랜치 최신 코드와 동일)
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new IllegalArgumentException("file.required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Material material = Material.builder()
                .title(metadata.title())
                .description(metadata.description())
                .professorName(metadata.professorName())
                .courseName(metadata.courseName())
                .year(metadata.year())
                .semester(metadata.semester())
                // .filePath( ... ) // TODO: filePath 관련 로직이 Material 엔티티와 DTO에 있으나, 빌더에 빠져있음 (원본 코드)
                // .courseDivision( ... ) // TODO: (원본 코드)
                .user(user)
                .build();

        Material savedMaterial = materialRepository.save(material);

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                AttachmentDto attachmentInfo = attachmentService.saveFileAndGetInfo(file);

                Attachment attachment = Attachment.builder()
                        .originalFileName(attachmentInfo.getOriginalFileName())
                        .storedFilePath(attachmentInfo.getStoredFilePath())
                        .material(savedMaterial) // 빌더에서 바로 Material 설정
                        .build();
                
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
        // ('옥민희'님 코드 - `develop` 브랜치 최신 코드와 동일)
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        return MaterialGetResponseDto.fromEntity(material);
    }

    @Transactional
    public void deleteMaterial(Long materialId, Long userId) {
        // ('이건휘'님 코드 - `develop` 브랜치 최신 코드와 동일)
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
        // ('이건휘'님 코드 - `develop` 브랜치 최신 코드와 동일)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
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
        // ('이건휘'님 코드 - `develop` 브랜치 최신 코드와 동일)
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
        // ('이건휘'님 코드 - `develop` 브랜치 최신 코드와 동일)
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, 10, sort);
        Page<Material> materialsPage = materialRepository.findByUserId(userId, pageable);
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
    public AttachmentDownloadDto downloadMaterial(Long materialId, Long attachmentId, Long userId) throws IOException {
        
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new NotFoundException("Material", materialId));

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment", attachmentId));

        if (!attachment.getMaterial().getId().equals(materialId)) {
            throw new IllegalArgumentException("해당 자료에 속한 파일이 아닙니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        // --- ▼▼▼ 2. '구매 내역 저장' 로직 수정/추가 ▼▼▼ ---
        if (!material.getUser().getId().equals(userId)) { // 본인 자료가 아니라면
            
            // 중복 구매 확인
            boolean alreadyPurchased = userMaterialRepository.existsByUserAndMaterial(user, material);
            if (!alreadyPurchased) { // 구매 내역이 없다면
                
                // 포인트 차감
                pointService.updatePoints(
                        user.getEmail(),
                        200, // (임시) 200 포인트
                        "자료 다운로드: " + material.getTitle(),
                        PointHistory.PointType.USE
                );

                // 구매 내역 저장 (이 로직이 빠져있었음!)
                UserMaterial purchaseRecord = new UserMaterial();
                purchaseRecord.setUser(user);
                purchaseRecord.setMaterial(material);
                userMaterialRepository.save(purchaseRecord);
            }
            // 이미 구매한 사람이면 (alreadyPurchased == true) 포인트 차감 없이 다운로드 진행
        }
        // --- ▲▲▲ 여기까지 수정/추가 ▲▲▲ ---

        Path filePath = Paths.get(attachment.getStoredFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 읽을 수 없습니다: " + attachment.getOriginalFileName());
        }

        return AttachmentDownloadDto.builder()
                .resource(resource)
                .originalFileName(attachment.getOriginalFileName())
                .fileSize(Files.size(filePath))
                .contentType(Files.probeContentType(filePath))
                .build();
    }

    // ▼▼▼ 3. '다운로드 목록 조회' 메소드 (이건휘) 새로 추가 ▼▼▼
    public MaterialListResponseDto getMyDownloadedMaterials(Long userId, int page) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        Sort sort = Sort.by(Sort.Direction.DESC, "id"); // 구매 순서 (최신순)
        Pageable pageable = PageRequest.of(page - 1, 10, sort);

        // 1. UserMaterialRepository에서 구매 내역 조회
        Page<UserMaterial> purchases = userMaterialRepository.findByUserWithMaterial(user, pageable);

        // 2. 구매 내역(UserMaterial)에서 족보(Material) 정보만 꺼내서 DTO로 변환
        List<MaterialSummaryDto> materialDtos = purchases.getContent().stream()
                .map(userMaterial -> MaterialSummaryDto.from(userMaterial.getMaterial()))
                .collect(Collectors.toList());

        // 3. 페이지 정보
        PageInfo pageInfo = new PageInfo(
                purchases.getNumber() + 1,
                purchases.getTotalPages(),
                purchases.getTotalElements()
        );

        return new MaterialListResponseDto(pageInfo, materialDtos);
    }
}
