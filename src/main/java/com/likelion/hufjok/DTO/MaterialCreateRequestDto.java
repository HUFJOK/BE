package com.likelion.hufjok.DTO;

import com.likelion.hufjok.domain.Material;
import com.likelion.hufjok.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class MaterialCreateRequestDto {

    @NotBlank(message = "제목은 입력해주세요.")
    private String title;

    @NotBlank(message = "설명을 입력해주세요.")
    private String description;

    @NotBlank(message = "교수명을 입력해주세요.")
    private String professorName;

    @NotBlank(message = "강의명을 입력해주세요.")
    private String courseName;

    @NotNull(message = "연도를 입력해주세요.")
    private int year;

    @NotBlank(message = "학기를 입력해주세요.")
    private int semester;

    public Material toEntity(User user) {
        return Material.builder()
                .title(this.title)
                .description(this.description)
                .professorName(this.professorName)
                .courseName(this.courseName)
                .year(this.year)
                .semester(this.semester)
                .user(user)
                .build();
    }
}