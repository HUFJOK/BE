package com.likelion.hufjok.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    @Value("${file.dir}")
    private String fileDir; // 예: ./files 또는 /var/data/files

    /** fileDir + filename을 안전하게 합침 */
    public Path getFullPath(String filename) {
        return Paths.get(fileDir, filename);
    }

    /** 여러 파일 저장 */
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> result = new ArrayList<>();
        for (MultipartFile mf : multipartFiles) {
            if (!mf.isEmpty()) {
                result.add(storeFile(mf)); // 한 번만!
            }
        }
        return result;
    }

    /** 단일 파일 저장 */
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) return null;

        String originalFilename = multipartFile.getOriginalFilename();
        String storedFileName   = createStoreFileName(originalFilename);

        Path destPath = getFullPath(storedFileName);

        // 부모 디렉토리 보장
        Files.createDirectories(destPath.getParent());

        // 저장 (Path 또는 File 모두 가능)
        multipartFile.transferTo(destPath);

        return new UploadFile(originalFilename, storedFileName); // DB엔 파일명만 저장(권장)
    }

    private String createStoreFileName(String originalFilename) {
        String ext = extractExt(originalFilename); // "pdf"
        String uuid = UUID.randomUUID().toString();
        return (ext.isEmpty()) ? uuid : uuid + "." + ext;
    }

    private String extractExt(String originalFilename) {
        if (originalFilename == null) return "";
        int pos = originalFilename.lastIndexOf('.');
        return (pos == -1) ? "" : originalFilename.substring(pos + 1);
    }
}
