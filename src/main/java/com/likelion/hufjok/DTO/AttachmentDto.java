package com.likelion.hufjok.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttachmentDto {
    private String originalFileName;
    private String storedFilePath;
}
