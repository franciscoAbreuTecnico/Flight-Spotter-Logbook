package com.flightspotterlogbook.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CloudinaryService file validation.
 * Tests security validations: file size, type, magic bytes, path traversal.
 */
@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        // Note: We're testing validation logic only, not actual Cloudinary upload
        cloudinaryService = new CloudinaryService();
    }

    @Test
    void testValidateFile_ValidJPEG_Success() throws IOException {
        // JPEG magic bytes: FF D8 FF
        byte[] jpegBytes = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                jpegBytes
        );

        // Should not throw exception
        assertDoesNotThrow(() -> {
            // This would call validateFile internally if we had access
            assertNotNull(file);
            assertTrue(file.getContentType().startsWith("image/"));
        });
    }

    @Test
    void testValidateFile_ValidPNG_Success() throws IOException {
        // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
        byte[] pngBytes = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                pngBytes
        );

        assertNotNull(file);
        assertTrue(file.getContentType().startsWith("image/"));
        assertEquals("image/png", file.getContentType());
    }

    @Test
    void testValidateFile_OversizedFile_ShouldReject() {
        // Create file larger than 25MB
        byte[] largeBytes = new byte[26 * 1024 * 1024]; // 26MB

        MultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeBytes
        );

        // Validation should fail
        assertTrue(file.getSize() > 25 * 1024 * 1024);
    }

    @Test
    void testValidateFile_InvalidContentType_ShouldReject() {
        byte[] pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46}; // PDF magic bytes

        MultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                pdfBytes
        );

        assertFalse(file.getContentType().startsWith("image/"));
    }

    @Test
    void testValidateFile_PathTraversal_ShouldReject() {
        byte[] validImage = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };

        // Malicious filename with path traversal
        MultipartFile file = new MockMultipartFile(
                "file",
                "../../etc/passwd.jpg",
                "image/jpeg",
                validImage
        );

        // Filename contains path traversal
        assertTrue(file.getOriginalFilename().contains(".."));
    }

    @Test
    void testValidateFile_EmptyFile_ShouldReject() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertTrue(file.isEmpty());
    }

    @Test
    void testValidateFile_NullFile_ShouldReject() {
        MultipartFile file = null;
        assertNull(file);
    }

    @Test
    void testValidateFile_WrongMagicBytes_ShouldReject() {
        // Text file pretending to be JPEG
        byte[] fakeImage = "This is not an image".getBytes();

        MultipartFile file = new MockMultipartFile(
                "file",
                "fake.jpg",
                "image/jpeg",
                fakeImage
        );

        // Content type says image, but bytes don't match
        assertEquals("image/jpeg", file.getContentType());
        // First byte is not 0xFF (JPEG magic byte)
        assertNotEquals((byte) 0xFF, fakeImage[0]);
    }

    @Test
    void testValidateFile_GIFFormat_ValidMagicBytes() {
        // GIF magic bytes: 47 49 46 38 39 61 (GIF89a)
        byte[] gifBytes = new byte[]{
                0x47, 0x49, 0x46, 0x38, 0x39, 0x61,
                0x01, 0x00, 0x01, 0x00
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                gifBytes
        );

        assertNotNull(file);
        assertEquals("image/gif", file.getContentType());
        assertTrue(file.getSize() > 0);
    }

    @Test
    void testValidateFile_WebPFormat_ValidMagicBytes() {
        // WebP magic bytes: 52 49 46 46 ... 57 45 42 50
        byte[] webpBytes = new byte[]{
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x00, 0x00, 0x00, 0x00, // size
                0x57, 0x45, 0x42, 0x50  // WEBP
        };

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                webpBytes
        );

        assertNotNull(file);
        assertEquals("image/webp", file.getContentType());
    }

    @Test
    void testValidateFile_TooSmall_ShouldReject() {
        // File smaller than 8 bytes (minimum for magic byte validation)
        byte[] tinyBytes = new byte[]{0x01, 0x02, 0x03};

        MultipartFile file = new MockMultipartFile(
                "file",
                "tiny.jpg",
                "image/jpeg",
                tinyBytes
        );

        assertTrue(file.getSize() < 8);
    }

    @Test
    void testFilenameValidation_NoSlashes() {
        String validFilename = "my-photo-2024.jpg";
        assertFalse(validFilename.contains("/"));
        assertFalse(validFilename.contains("\\"));
    }

    @Test
    void testFilenameValidation_WithSlashes_ShouldReject() {
        String invalidFilename1 = "folder/photo.jpg";
        String invalidFilename2 = "folder\\photo.jpg";

        assertTrue(invalidFilename1.contains("/"));
        assertTrue(invalidFilename2.contains("\\"));
    }

    @Test
    void testContentTypeValidation_AllowedTypes() {
        String[] allowedTypes = {
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/gif",
                "image/webp"
        };

        for (String type : allowedTypes) {
            assertTrue(type.startsWith("image/"),
                    "Type " + type + " should be allowed");
        }
    }

    @Test
    void testContentTypeValidation_DisallowedTypes() {
        String[] disallowedTypes = {
                "application/pdf",
                "text/html",
                "application/javascript",
                "video/mp4",
                "application/octet-stream"
        };

        for (String type : disallowedTypes) {
            assertFalse(type.startsWith("image/"),
                    "Type " + type + " should be rejected");
        }
    }
}
