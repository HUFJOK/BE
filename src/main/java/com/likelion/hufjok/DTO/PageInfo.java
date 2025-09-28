package com.likelion.hufjok.DTO;

public record PageInfo(
        int currentPage,
        int totalPages,
        long totalCount
) {}