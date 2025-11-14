package com.likelion.hufjok.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.hufjok.DTO.*;
import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.service.MaterialService;
import com.likelion.hufjok.service.NotFoundException;
import com.likelion.hufjok.service.ReviewService;
import com.likelion.hufjok.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map; // <-- Map import 추가

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final ObjectMapper objectMapper;
    private final UserService userService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "새 자료 작성 및 파일 업로드",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = MaterialCreateMultipartDoc.class),

                            encoding = {
                                    @Encoding(name = "metadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    })
    public ResponseEntity<MaterialCreateResponseDto> createMaterialMultipart(
            @RequestPart(value = "metadata") String metadataJson,
            @RequestPart(value = "files") List<MultipartFile> files,
            @AuthenticationPrincipal OAuth2User principal
    ) throws IOException {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        MaterialCreateRequestDto metadata;
        try {
            metadata = objectMapper.readValue(metadataJson, MaterialCreateRequestDto.class);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "metadata(JSON) 형식이 올바르지 않습니다.", e);
        }

        // ---- 필수 필드 검증 (null → DB 에러 방지) ----
        if (metadata.courseName() == null || metadata.courseName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseName은 필수입니다.");
        }
        if (metadata.courseDivision() == null || metadata.courseDivision().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "courseDivision은 필수입니다.");
        }
        if (metadata.title() == null || metadata.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title은 필수입니다.");
        }
        if (metadata.year() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year는 필수입니다.");
        }
        if (metadata.semester() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "semester는 필수입니다.");
        }

        MaterialCreateResponseDto response =
                materialService.createMaterial(userId, metadata, files);

        return ResponseEntity
                .created(URI.create("/api/v1/materials/" + response.getMaterialId()))
                .body(response);
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
            @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        // 이메일 꺼내기
        String email = principal.getAttribute("email");

        // 이메일로 User 찾아서 userId 얻기
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        // 이제 userId 전달
        MaterialUpdateResponseDto response = materialService.updateMaterial(materialId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}")
    @Operation(summary = "자료 삭제")
    public ResponseEntity<Void> deleteMaterial(
            @PathVariable Long materialId,
            @AuthenticationPrincipal OAuth2User principal // 👈 타입 변경
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        // ✅ ID 추출 로직 추가
        final String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        materialService.deleteMaterial(materialId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{materialId}/attachments/{attachmentId}/purchase")
    @Operation(
            summary = "자료 구매",
            description = "자료를 구매합니다. 본인이 업로드한 자료인 경우 포인트가 차감되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구매 성공"),
            @ApiResponse(responseCode = "400", description = "포인트 부족 (200P 필요)"),
            @ApiResponse(responseCode = "404", description = "자료 또는 파일을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 구매한 자료입니다.")
    })
    public ResponseEntity<?> purchaseMaterial(
            @PathVariable Long materialId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));

        try {
            materialService.purchaseMaterial(materialId, attachmentId, userId);
            return ResponseEntity.ok(Map.of("message", "구매가 완료되었습니다."));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (ResponseStatusException e) {
            // 400 / 409 등 그대로 전달
            throw e;
        }
    }


    // ▼▼▼ '옥민희'님 파트 - '자료 구매' API (수정됨) ▼▼▼
    @GetMapping("/{materialId}/download/{attachmentId}") // <-- attachmentId 추가
    @Operation(
            summary = "자료 파일 다운로드",
            description = "이미 구매한 자료이거나 본인이 업로드한 자료를 다운로드합니다. 다운로드 시 포인트는 차감되지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "다운로드 성공"),
            @ApiResponse(responseCode = "403", description = "구매하지 않은 자료입니다."),
            @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
    })
    public ResponseEntity<?> downloadMaterial( // ResponseEntity<Resource> -> ResponseEntity<?>
            @Parameter(description = "자료 ID", required = true)
            @PathVariable Long materialId,

            @Parameter(description = "첨부파일 ID", required = true)
            @PathVariable Long attachmentId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal OAuth2User principal
    ) throws IOException {
        
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "등록되지 않은 사용자입니다."));
        
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
        } catch (IllegalArgumentException e) { // 잘못된 요청(자료-첨부파일 매칭 오류 등)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    static class MaterialCreateMultipartDoc {
        @Schema(description = "자료 메타데이터(JSON)")
        public MaterialCreateRequestDto metadata;

        @ArraySchema(arraySchema = @Schema(description = "업로드할 파일들"),
                schema = @Schema(type = "string", format = "binary"))
        public List<MultipartFile> files;
    }

}
