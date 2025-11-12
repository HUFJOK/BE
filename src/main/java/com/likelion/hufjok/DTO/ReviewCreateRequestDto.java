package com.likelion.hufjok.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewCreateRequestDto {
    private Long materialId;
    private String comment;
    private int rating;
    private int reviewCount;
}