package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.*; // User, Material, Attachment, PointHistory, UserMaterial
import com.likelion.hufjok.file.UploadFile;
import com.likelion.hufjok.repository.*; // 5개 Repository
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.likelion.hufjok.file.FileStore;
import org.springframework.web.server.ResponseStatusException;

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
    private final FileStore fileStore;

    // ▼▼▼ 1. '구매 내역' 저장을 위해 추가 ▼▼▼
    private final UserMaterialRepository userMaterialRepository;
    private static final long MB = 1024 * 1024;
    private static final long MAX_FILE_SIZE_BYTES    = 200 * MB; // 개별 파일 최대 200MB
    private static final long MAX_REQUEST_SIZE_BYTES = 200 * MB;

    @Transactional
    public MaterialCreateResponseDto createMaterial(
            Long userId,
            @Valid MaterialCreateRequestDto metadata,
            List<MultipartFile> files
    ) throws IOException {

        // 1) 인증 & 입력 검증
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 비어 있습니다.");
        }

        // (선택) PDF만 허용
        for (MultipartFile f : files) {
            if (f.isEmpty()) continue;
            String name = f.getOriginalFilename();
            String ct   = (f.getContentType() == null) ? "" : f.getContentType().toLowerCase();
            boolean pdfByName = (name != null && name.toLowerCase().endsWith(".pdf"));
            boolean pdfByCt   = ct.contains("pdf");
            if (!(pdfByName || pdfByCt)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "PDF 파일만 업로드 가능합니다: " + name);
            }
        }

        // 2) 용량 선검증(옵션) — 네가 넣어둔 로깅 그대로 활용해도 됨
        long totalBytes = 0L;
        final long MB = 1024 * 1024;
        final long MAX_FILE = 200L * MB;
        final long MAX_REQ  = 200L * MB;
        for (MultipartFile f : files) {
            long sz = f.getSize();
            totalBytes += sz;
            if (sz > MAX_FILE) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "개별 파일이 200MB를 초과: " + f.getOriginalFilename());
            }
        }
        if (totalBytes > MAX_REQ) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "요청 총 용량이 200MB를 초과");
        }

        // 3) 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));

        // 4) 자료(메타데이터) 저장
        Material material = Material.builder()
                .title(metadata.title())
                .description(metadata.description())
                .professorName(metadata.professorName())
                .courseName(metadata.courseName())
                .courseDivision(metadata.courseDivision())
                .year(metadata.year())
                .semester(metadata.semester())
                .description(metadata.description())
                .grade(metadata.grade() != null ? metadata.grade() : "미정")
                .user(user)
                .build();
        Material savedMaterial = materialRepository.save(material);

        // 5) 파일 저장 + 첨부 엔티티 저장
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            AttachmentDto info = attachmentService.saveFileAndGetInfo(file);
            Attachment attachment = Attachment.builder()
                    .originalFileName(info.getOriginalFileName())
                    .storedFilePath(info.getStoredFilePath()) // DB에는 파일명만
                    .material(savedMaterial)
                    .build();
            attachmentRepository.save(attachment);
        }

        // 6) 포인트 적립
        pointService.updatePoints(
                user.getEmail(),
                200,
                "자료 게시: " + savedMaterial.getTitle(),
                PointHistory.PointType.EARN
        );

        // 7) 응답
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
//        Resource resource = new UrlResource(filePath.toUri());

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            throw new IOException("파일을 읽을 수 없습니다. 경로: " +  filePath.toAbsolutePath() + ", 파일명: " + attachment.getOriginalFileName());
        }

        // Path로 Resource 생성하기
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 읽을 수 없습니다. 경로: " + filePath.toAbsolutePath());
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
