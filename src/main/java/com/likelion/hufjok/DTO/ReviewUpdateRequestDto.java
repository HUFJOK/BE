package com.likelion.hufjok.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewUpdateRequestDto {

    private int rating;

    private String comment;
}
