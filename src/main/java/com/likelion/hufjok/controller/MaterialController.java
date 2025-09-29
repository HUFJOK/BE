package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.service.MaterialService;
import com.likelion.hufjok.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/{materialId}/review")
    public ResponseEntity<ReviewCreateResponseDto> createReview(
            @PathVariable Long materialId,
            @Valid @RequestBody ReviewCreateRequestDto request
    ) {
        Long userId = 1L; // 임시 사용자 ID
        ReviewCreateResponseDto response = reviewService.createReview(materialId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{materialId}")
    public ResponseEntity<MaterialUpdateResponseDto> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialUpdateRequestDto request
    ) {
        Long userId = 1L; // 임시 사용자 ID
        MaterialUpdateResponseDto response = materialService.updateMaterial(materialId, userId, request);
        return ResponseEntity.ok(response);
    }

    // --- 이 메소드를 클래스 안으로 옮겼습니다 ---
    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long materialId
    ) {
        Long userId = 1L; // 임시 사용자 ID
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