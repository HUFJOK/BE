package com.likelion.todolist.DTO;

import jakarta.validation.constraints.NotNull;

public record TodoDoneReqDto(
        @NotNull
        Boolean completed
) {}
