package com.likelion.hufjok.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;


public record MaterialCreateRequestDto(
        String title,
        String description,
        String professorName,
        String courseName,
        Integer year,
        Integer semester,
        String courseDivision,
        String major,
        String grade
) {}
