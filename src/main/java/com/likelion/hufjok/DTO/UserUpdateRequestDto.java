package com.likelion.hufjok.DTO;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserUpdateRequestDto {

    private String nickname;
    private String major;
    private String doubleMajor;
    private String minor;
}
