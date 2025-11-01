package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Point;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointResponseDto {
    private int amount;
    private String reason;
    private LocalDateTime createdAt;
    private String email;

    public static PointResponseDto from(Point point){
        return PointResponseDto.builder()
                .amount(point.getAmount())
                .reason(point.getReason())
                .createdAt(point.getCreatedAt())
                .email(point.getUser().getEmail())
                .build();
    }
}
