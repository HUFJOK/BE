package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private String nickname;
    private String major;
    private String doubleMajor;
    private String minor;
    private String email;

    public UserResponseDto(String nickname, String major, String doubleMajor, String minor, String email) {
        this.nickname = nickname;
        this.major = major;
        this.doubleMajor = doubleMajor;
        this.minor = minor;
        this.email = email;
    }

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .nickname(user.getNickname())
                .major(user.getMajor())
                .doubleMajor(user.getDoubleMajor())
                .minor(user.getMinor())
                .email(user.getEmail())
                .build();
    }
}
