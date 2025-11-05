package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.service.MaterialService;
import com.likelion.hufjok.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.net.URI;
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
    @Operation(summary = "자료 조회")
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

    @PutMapping("/{materialId}") // <-- 수정된 부분
    @Operation(summary = "자료 수정")
    public ResponseEntity<MaterialUpdateResponseDto> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialUpdateRequestDto request,
            @AuthenticationPrincipal Long userId
    ) {
        MaterialUpdateResponseDto response = materialService.updateMaterial(materialId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}") // <-- 수정된 부분
    @Operation(summary = "자료 삭제")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal Long userId
    ) {
        materialService.deleteMaterial(materialId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "자료 정보 및 파일 업로드")
    public ResponseEntity<MaterialCreateResponseDto> createMaterialMultipart(
            @RequestPart("metadata") @Valid MaterialCreateRequestDto metadata,
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        MaterialCreateResponseDto response = materialService.createMaterial(userId, metadata, files);

        return ResponseEntity
                .created(URI.create("/api/v1/materials" + response.getMaterialId()))
                .body(response);
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
