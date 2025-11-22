package com.example.community.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    // 기본 업로드 루트 디렉토리 (프로젝트 루트 기준 ./uploads)
    private final Path uploadRootDir;

    public LocalFileStorageService(
            @Value("${app.upload.root-dir:uploads}") String uploadRootDir
    ) {
        this.uploadRootDir = Paths.get(uploadRootDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // 디렉토리: uploads/posts, uploads/profiles 등
            Path dirPath = uploadRootDir.resolve(directory).normalize();
            Files.createDirectories(dirPath);

            // 원본 확장자 유지
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null) {
                int dot = originalFilename.lastIndexOf('.');
                if (dot >= 0) {
                    ext = originalFilename.substring(dot);
                }
            }

            String filename = UUID.randomUUID().toString() + ext;
            Path target = dirPath.resolve(filename).normalize();

            file.transferTo(target.toFile());

            // 프론트에서 사용할 URL path (ResourceHandler에서 /uploads/** 로 매핑)
            return "/uploads/" + directory + "/" + filename;
        } catch (IOException e) {
            // 필요하면 로깅 + 커스텀 예외로 감싸기
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }
}
