package com.likelion.hufjok.DTO;

public record MaterialUpdateRequestDto(
        String title,
        String description,
        String professorName,
        String courseName,
        Integer year,
        Integer semester,
        String courseDivision,
        String grade
) {}