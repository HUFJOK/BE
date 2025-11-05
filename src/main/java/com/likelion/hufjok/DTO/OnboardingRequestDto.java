package com.likelion.hufjok.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class OnboardingRequestDto {
    private String major;
    private String doubleMajor;
    private String minor;
}
