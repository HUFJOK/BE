package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.NotNull;

public record TodoDoneReqDto(
        @NotNull
        Boolean completed
) {}
