package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingResponseDto {
    private String major;
    private String doubleMajor;
    private String minor;

    public static OnboardingResponseDto from(User user) {
        return OnboardingResponseDto.builder()
                .major(user.getMajor())
                .doubleMajor(user.getDoubleMajor())
                .minor(user.getMinor())
                .build();
    }
}
