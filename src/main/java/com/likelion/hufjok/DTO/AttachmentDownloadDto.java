package com.likelion.hufjok.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
@AllArgsConstructor
public class AttachmentDownloadDto {
    private Resource resource;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
}
