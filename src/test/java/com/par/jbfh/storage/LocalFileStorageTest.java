package com.par.jbfh.storage;

import com.par.jbfh.storage.enums.FileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage fileStorage;

    @BeforeEach
    void setUp() {
        fileStorage = new LocalFileStorage(tempDir.toString());
        fileStorage.init();
    }

    @Test
    void validateShouldPassForValidFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("image/jpeg");

        fileStorage.validate(file, FileType.CLUB_LOGO);

        // no exception thrown
    }

    @Test
    void validateShouldThrowWhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> fileStorage.validate(file, FileType.CLUB_LOGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    void validateShouldThrowWhenFileExceedsMaxSize() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(300 * 1024L); // > 200KB
        when(file.getContentType()).thenReturn("image/jpeg");

        assertThatThrownBy(() -> fileStorage.validate(file, FileType.CLUB_LOGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    void validateShouldThrowWhenContentTypeNotAllowed() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThatThrownBy(() -> fileStorage.validate(file, FileType.CLUB_LOGO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File type 'application/pdf' is not allowed");
    }

    @Test
    void saveShouldStoreFileAndReturnPath() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("test.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));

        UUID entityId = UUID.randomUUID();
        String savedPath = fileStorage.save(file, entityId, FileType.CLUB_LOGO);

        assertThat(savedPath).isNotNull();
        assertThat(Files.exists(Path.of(savedPath))).isTrue();
    }

    @Test
    void deleteShouldRemoveFile() throws IOException {
        Path filePath = tempDir.resolve("test.txt");
        Files.writeString(filePath, "test");

        fileStorage.delete(filePath.toString());

        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void deleteShouldNotThrowWhenFileDoesNotExist() {
        fileStorage.delete("/nonexistent/path/file.png");

        // no exception
    }

    @Test
    void deleteShouldNotThrowWhenPathIsNull() {
        fileStorage.delete(null);

        // no exception
    }

    @Test
    void deleteShouldNotThrowWhenPathIsBlank() {
        fileStorage.delete("   ");

        // no exception
    }

    @Test
    void getResourceShouldReturnResourceForExistingFile() throws IOException {
        Path filePath = tempDir.resolve("resource.txt");
        Files.writeString(filePath, "test content");

        Resource resource = fileStorage.getResource(filePath.toString());

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void getResourceShouldThrowWhenFileNotFound() {
        assertThatThrownBy(() -> fileStorage.getResource("/nonexistent/file.txt"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void getResourceShouldThrowWhenPathIsNull() {
        assertThatThrownBy(() -> fileStorage.getResource(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File path is null or empty");
    }

    @Test
    void getResourceShouldThrowWhenPathIsBlank() {
        assertThatThrownBy(() -> fileStorage.getResource("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File path is null or empty");
    }
}