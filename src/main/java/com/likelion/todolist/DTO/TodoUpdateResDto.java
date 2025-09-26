package com.likelion.todolist.DTO;

import java.time.LocalDateTime;

public record TodoUpdateResDto(
        Long id,
        String title,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
