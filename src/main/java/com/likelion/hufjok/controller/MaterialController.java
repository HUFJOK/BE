package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.service.MaterialService;
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
        MaterialUpdateResponseDto response = materialService.updateMaterial(materialId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}")
    @Operation(summary = "자료 삭제")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal Long userId
    ) {
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
        MaterialCreateResponseDto response = materialService.createMaterial(userId, metadata, files);

        return ResponseEntity
                .created(URI.create("/api/v1/materials/" + response.getMaterialId()))
                .body(response);
    }

    @GetMapping("/{materialId}/download")
    @Operation(
            summary = "자료 파일 다운로드",
            description = "자료를 다운로드합니다. 본인이 업로드한 자료가 아닌 경우 200 포인트가 차감됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "400", description = "포인트 부족 (200P 필요)"),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    public ResponseEntity<Resource> downloadMaterial(
            @Parameter(description = "자료 ID", required = true)
            @PathVariable Long materialId,

            @Parameter(description = "첨부파일 ID", required = true)
            @PathVariable Long attachmentId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        AttachmentDownloadDto fileDownload = materialService.downloadMaterial(materialId, attachmentId, userId);

        String encodedFileName = URLEncoder.encode(fileDownload.getOriginalFileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedFileName + "\"")
                .body(fileDownload.getResource());
    }

    @GetMapping("/me/materials")
    @Operation(summary = "내가 올린 자료 목록 조회")
    public ResponseEntity<MaterialListResponseDto> getMyUploadedMaterials(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MaterialListResponseDto result = materialService.getMyUploadedMaterials(userId, page);
        return ResponseEntity.ok(result);
    }
}
