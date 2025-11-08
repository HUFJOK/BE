package com.likelion.hufjok.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;


public record MaterialCreateRequestDto(
        @NotBlank String title,
        String description,
        @NotBlank String professorName,
        @NotBlank String courseName,
        @NotBlank @JsonAlias({"courseDivision", "course_division"}) String courseDivision,
        @Min(2000) Integer year,
        @Min(1) @Max(2) Integer semester
) {}
