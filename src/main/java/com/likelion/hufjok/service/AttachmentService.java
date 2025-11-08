package com.likelion.hufjok.service;

import com.likelion.hufjok.DTO.AttachmentDto;
import com.likelion.hufjok.domain.Attachment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AttachmentService { // FileService -> AttachmentService

    @Value("${file.dir}")
    private String fileDir;
    public AttachmentDto saveFileAndGetInfo(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = uuid + extension;

        Path fullPath = Paths.get(fileDir + storedFilename);

        if (!Files.exists(fullPath.getParent())) {
            Files.createDirectories(fullPath.getParent());
        }

        file.transferTo(fullPath);

        return AttachmentDto.builder()
                .originalFileName(originalFilename)
                .storedFilePath(fullPath.toString())
                .build();
    }

    public void deleteFileByPath(String fullPathString) throws IOException {
        if (fullPathString == null || fullPathString.isEmpty()) {
            return;
        }

        Path fullPath = Paths.get(fullPathString);

        if (Files.exists(fullPath)) {
            Files.delete(fullPath);
        }
    }

    public Resource downloadFile(String storedFilename) throws MalformedURLException {
        Path fullPath = Paths.get(fileDir + storedFilename);
        Resource resource = new UrlResource(fullPath.toUri());

        if (!resource.exists() || !resource.isFile()) {
            throw new IllegalArgumentException("다운로드할 파일을 찾을 수 없습니다: " + storedFilename);
        }

        return resource;
    }

}