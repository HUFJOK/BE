package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaterialUpdateRequestDto(
        @NotBlank
        @Size(max = 100)
        String title,

        String description // description은 비워도 되도록 @NotBlank 제외
) {}