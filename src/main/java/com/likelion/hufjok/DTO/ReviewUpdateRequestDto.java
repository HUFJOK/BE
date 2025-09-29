package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewUpdateRequestDto(
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
        int rating,

        @Size(max = 1000, message = "후기 내용은 1000자를 초과할 수 없습니다.")
        String comment
) {}