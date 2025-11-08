package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.service.MaterialService;
import com.likelion.hufjok.service.NotFoundException;
import com.likelion.hufjok.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Parameter;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map; // <-- Map import 추가

@RestController
@RequestMapping("/api/v1/materials")
public class MaterialController {

    private final MaterialService materialService;
    private final ReviewService reviewService;

    public MaterialController(MaterialService materialService, ReviewService reviewService) {
        this.materialService = materialService;
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "자료 게시물 목록 조회")
    public ResponseEntity<?> getMaterials(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        var result = materialService.getMaterials(keyword, year, semester, sortBy, page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{materialId}")
    @Operation(summary = "특정 자료 상세 조회")
    public ResponseEntity<MaterialGetResponseDto> getMaterialDetail(
            @PathVariable Long materialId
    ) {
        MaterialGetResponseDto response = materialService.getMaterial(materialId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{materialId}")
    @Operation(summary = "자료 수정")
    public ResponseEntity<MaterialUpdateResponseDto> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialUpdateRequestDto request,
            @AuthenticationPrincipal Long userId
    ) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MaterialUpdateResponseDto response = materialService.updateMaterial(materialId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}")
    @Operation(summary = "자료 삭제")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal Long userId
    ) {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        materialService.deleteMaterial(materialId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "새 자료 작성 및 파일 업로드")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    })
    public ResponseEntity<MaterialCreateResponseDto> createMaterialMultipart(
            @Parameter(description = "자료 메타데이터", required = true)
            @RequestPart("metadata") @Valid MaterialCreateRequestDto metadata,

            @Parameter(description = "업로드할 파일 목록", required = true)
            @RequestPart("files") List<MultipartFile> files,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MaterialCreateResponseDto response = materialService.createMaterial(userId, metadata, files);

        return ResponseEntity
                .created(URI.create("/api/v1/materials/" + response.getMaterialId()))
                .body(response);
    }

    // ▼▼▼ '옥민희'님 파트 - '자료 구매' API (수정됨) ▼▼▼
    @GetMapping("/{materialId}/download/{attachmentId}") // <-- attachmentId 추가
    @Operation(
            summary = "자료 파일 다운로드 (자료 구매)",
            description = "자료를 다운로드합니다. 본인이 업로드한 자료가 아닌 경우 200 포인트가 차감됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "400", description = "포인트 부족 (200P 필요)"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 구매한 자료입니다. (이젠 발생 안 함)")
    })
    public ResponseEntity<?> downloadMaterial( // ResponseEntity<Resource> -> ResponseEntity<?>
            @Parameter(description = "자료 ID", required = true)
            @PathVariable Long materialId,

            @Parameter(description = "첨부파일 ID", required = true)
            @PathVariable Long attachmentId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        
        try {
            AttachmentDownloadDto fileDownload = materialService.downloadMaterial(materialId, attachmentId, userId);

            String encodedFileName = URLEncoder.encode(fileDownload.getOriginalFileName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileDownload.getContentType())) // 수정됨
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .body(fileDownload.getResource());
                    
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) { // 포인트 부족 또는 파일 ID 오류
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
