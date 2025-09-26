package com.likelion.hufjok.DTO;

import java.time.LocalDateTime;

public record TodoGetResDto(
        Long id,
        String title,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
