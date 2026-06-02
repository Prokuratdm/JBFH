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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalFileStorageTest {

    private LocalFileStorage fileStorage;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorage = new LocalFileStorage(tempDir.toString());
        fileStorage.init();
    }

    private MultipartFile createMockImageFile(String name, byte[] content, String contentType) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(content == null || content.length == 0);
        when(file.getSize()).thenReturn(content != null ? content.length : 0L);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getOriginalFilename()).thenReturn(name);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content != null ? content : new byte[0]));
        return file;
    }

    // --- validate ---

    @Test
    void validate_shouldPassForValidFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("image/jpeg");

        assertDoesNotThrow(() -> fileStorage.validate(file, FileType.CLUB_LOGO));
    }

    @Test
    void validate_shouldThrowWhenFileEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> fileStorage.validate(file, FileType.CLUB_LOGO));
    }

    @Test
    void validate_shouldThrowWhenFileTooLarge() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(300 * 1024L); // 300KB > 200KB limit
        when(file.getContentType()).thenReturn("image/jpeg");

        assertThrows(IllegalArgumentException.class, () -> fileStorage.validate(file, FileType.CLUB_LOGO));
    }

    @Test
    void validate_shouldThrowWhenWrongContentType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(IllegalArgumentException.class, () -> fileStorage.validate(file, FileType.CLUB_LOGO));
    }

    @Test
    void validate_shouldThrowWhenContentTypeNull() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(100L);
        when(file.getContentType()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> fileStorage.validate(file, FileType.CLUB_LOGO));
    }

    // --- save ---

    @Test
    void save_shouldSaveFileSuccessfully() throws IOException {
        byte[] content = "fake-image-content".getBytes();
        MultipartFile file = createMockImageFile("logo.jpg", content, "image/jpeg");
        UUID entityId = UUID.randomUUID();

        String savedPath = fileStorage.save(file, entityId, FileType.CLUB_LOGO);

        assertNotNull(savedPath);
        assertTrue(savedPath.contains(entityId.toString()));
        assertTrue(savedPath.endsWith(".jpg"));

        Path savedFile = Path.of(savedPath);
        assertTrue(Files.exists(savedFile));
        assertArrayEquals(content, Files.readAllBytes(savedFile));
    }

    @Test
    void save_shouldCreateSubdirectory() throws IOException {
        byte[] content = "test".getBytes();
        MultipartFile file = createMockImageFile("avatar.png", content, "image/png");
        UUID entityId = UUID.randomUUID();

        String savedPath = fileStorage.save(file, entityId, FileType.USER_AVATAR);

        assertTrue(savedPath.contains("avatars"));
        Path savedFile = Path.of(savedPath);
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void save_shouldHandleFileWithoutExtension() throws IOException {
        byte[] content = "test".getBytes();
        MultipartFile file = createMockImageFile("logo", content, "image/webp");
        UUID entityId = UUID.randomUUID();

        String savedPath = fileStorage.save(file, entityId, FileType.CLUB_LOGO);

        assertNotNull(savedPath);
        assertFalse(savedPath.endsWith("."));
    }

    // --- delete ---

    @Test
    void delete_shouldDeleteExistingFile() throws IOException {
        Path testFile = tempDir.resolve("logos/test_delete.jpg");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, "content");

        fileStorage.delete(testFile.toString());

        assertFalse(Files.exists(testFile));
    }

    @Test
    void delete_shouldNotThrowWhenFileNull() {
        assertDoesNotThrow(() -> fileStorage.delete(null));
        assertDoesNotThrow(() -> fileStorage.delete(""));
        assertDoesNotThrow(() -> fileStorage.delete("   "));
    }

    @Test
    void delete_shouldNotThrowWhenFileNotExists() {
        assertDoesNotThrow(() -> fileStorage.delete(tempDir.resolve("nonexistent.txt").toString()));
    }

    // --- getResource ---

    @Test
    void getResource_shouldReturnResourceForExistingFile() throws IOException {
        Path testFile = tempDir.resolve("logos/test_resource.jpg");
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, "resource content");

        Resource resource = fileStorage.getResource(testFile.toString());

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void getResource_shouldThrowWhenFileNotExists() {
        String path = tempDir.resolve("nonexistent.txt").toString();
        assertThrows(IllegalArgumentException.class, () -> fileStorage.getResource(path));
    }

    @Test
    void getResource_shouldThrowWhenPathNull() {
        assertThrows(IllegalArgumentException.class, () -> fileStorage.getResource(null));
        assertThrows(IllegalArgumentException.class, () -> fileStorage.getResource(""));
    }
}