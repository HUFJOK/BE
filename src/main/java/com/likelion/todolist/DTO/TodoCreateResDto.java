package com.likelion.todolist.DTO;

import java.time.LocalDateTime;

public record TodoCreateResDto(
        Long id,
        String title,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
