package com.likelion.todolist.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TodoUpdateReqDto(
        @NotBlank @Size(max = 200)
        String title,
        @NotNull
        Boolean completed
) {}
