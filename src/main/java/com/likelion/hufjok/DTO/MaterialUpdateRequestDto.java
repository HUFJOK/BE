package com.likelion.hufjok.DTO;

public record MaterialUpdateRequestDto(
        String title,
        String description,
        String professorName,
        String courseName,
        Integer year,
        Integer semester,
        String major,
        String courseDivision,
        String grade
) {}