package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {
    private String nickname;
    private String major;

    private String minor;
    private String email;
    private String errorMessage;


    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .major(user.getMajor())
                .minor(user.getMinor())
                .email(user.getEmail())
                .errorMessage(null)
                .build();
    }
}
