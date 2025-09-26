package com.likelion.todolist.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoCreateReqDto(
        @NotBlank @Size(max = 200)
        String title
) {}
