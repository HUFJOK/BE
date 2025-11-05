package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.*;

public record MaterialCreateRequestDto(
        @NotBlank String title,
        String description,
        @NotBlank String professorName,
        @NotBlank String courseName,
        @Min(2000) int year,
        @Min(1) @Max(2) int semester
) {}
