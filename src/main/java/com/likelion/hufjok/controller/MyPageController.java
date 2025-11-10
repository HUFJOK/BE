package com.likelion.hufjok.controller;

import com.likelion.hufjok.DTO.MaterialListResponseDto;
import com.likelion.hufjok.domain.User;
import com.likelion.hufjok.service.MaterialService;
import com.likelion.hufjok.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MaterialService materialService;
    private final UserService userService;

    @GetMapping("/materials")
    @Operation(
            summary = "내가 올린 자료 목록 조회",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<MaterialListResponseDto> getMyUploadedMaterials(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MaterialListResponseDto result = materialService.getMyUploadedMaterials(userId, page);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/downloads")
    @Operation(
            summary = "내가 다운로드(구매)한 자료 목록 조회",
            security = @SecurityRequirement(name = "Cookie Authentication")
    )
    public ResponseEntity<MaterialListResponseDto> getMyDownloadedMaterials(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(required = false, defaultValue = "1") int page
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = principal.getAttribute("email");
        Long userId = userService.findByEmail(email.toLowerCase())
                .map(User::getId)
                .orElse(null);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MaterialListResponseDto result = materialService.getMyDownloadedMaterials(userId, page);
        return ResponseEntity.ok(result);
    }
}
