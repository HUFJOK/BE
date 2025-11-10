package com.likelion.hufjok.DTO;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MaterialCreateMultipartDoc {
    private MaterialCreateRequestDto metadata;

    // @RequestPart("files")에 해당하는 파일 목록
    // Swagger UI에게 이 필드가 파일(MultipartFile) 목록임을 알려줍니다.
    private List<MultipartFile> files;

    // 중요: Lombok @Getter/@Setter를 사용하거나,
    // 명시적으로 기본 생성자(Default Constructor), Getter, Setter를 추가해야 합니다.
    // Lombok을 사용한다면 @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor 등을 추가합니다.

    // 예시: 명시적 Getter/Setter (Lombok 미사용 시)
    public MaterialCreateRequestDto getMetadata() { return metadata; }
    public void setMetadata(MaterialCreateRequestDto metadata) { this.metadata = metadata; }

    public List<MultipartFile> getFiles() { return files; }
    public void setFiles(List<MultipartFile> files) { this.files = files; }
}
