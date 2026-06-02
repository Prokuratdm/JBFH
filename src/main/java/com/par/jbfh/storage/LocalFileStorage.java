package com.par.jbfh.storage;

import com.par.jbfh.storage.enums.FileType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
public class LocalFileStorage implements FileStorage {

    private final Path basePath;

    public LocalFileStorage(@Value("${app.upload.base-path:uploads}") String basePath) {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(basePath);
            log.info("File storage initialized at: {}", basePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory: " + basePath, e);
        }
    }

    @Override
    public String save(MultipartFile file, UUID entityId, FileType fileType) {
        validate(file, fileType);

        try {
            Path uploadDir = basePath.resolve(fileType.getSubdirectory());
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = entityId + "_" + System.currentTimeMillis() + extension;
            Path targetPath = uploadDir.resolve(filename);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File saved: {} (size: {} bytes, type: {})", targetPath, file.getSize(), fileType);
            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save file for entity " + entityId, e);
        }
    }

    @Override
    public void delete(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", path);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }

    @Override
    public Resource getResource(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is null or empty");
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }
        return new FileSystemResource(path);
    }

    @Override
    public void validate(MultipartFile file, FileType fileType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > fileType.getMaxSizeBytes()) {
            throw new IllegalArgumentException(
                    "File size exceeds " + (fileType.getMaxSizeBytes() / 1024) + "KB limit. " +
                            "Current size: " + (file.getSize() / 1024) + "KB"
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || Arrays.stream(fileType.getAllowedContentTypes())
                .noneMatch(allowed -> allowed.equals(contentType))) {
            throw new IllegalArgumentException(
                    "File type '" + contentType + "' is not allowed. " +
                            "Allowed types: " + String.join(", ", fileType.getAllowedContentTypes())
            );
        }
    }
}