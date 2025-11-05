package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
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
import com.likelion.hufjok.DTO.MaterialCreateRequestDto;

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

    @PostMapping
    public ResponseEntity<MaterialResponseDto> createMaterial(
            // @AuthenticationPrincipal UserDetailsImpl userDetails, // 👈 최종적으로는 실제 사용자 정보를 사용
            @Valid @RequestBody MaterialCreateRequestDto request // 👈 2. 올바른 DTO로 변경
    ) {
        Long userId = 1L; // 임시 사용자 ID
        MaterialResponseDto response = materialService.createMaterial(userId, request); // 👈 3. 메소드 이름 수정
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
