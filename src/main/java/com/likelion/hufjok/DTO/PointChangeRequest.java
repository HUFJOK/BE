package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PointChangeRequest {
    @Min(1)
    private int amount;

    @NotBlank
    private String reason;
}
