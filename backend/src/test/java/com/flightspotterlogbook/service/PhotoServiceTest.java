package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.Photo;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.Visibility;
import com.flightspotterlogbook.repository.PhotoRepository;
import com.flightspotterlogbook.repository.SightingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PhotoService.
 * Tests photo upload, authorization, and Cloudinary integration.
 */
@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private PhotoService photoService;

    private Sighting testSighting;
    private String testUserId;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        testUserId = "user_123";
        testSighting = Sighting.builder()
                .id(1L)
                .ownerUserId(testUserId)
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("LAX")
                .visibility(Visibility.PUBLIC)
                .build();

        // Valid JPEG file
        byte[] jpegBytes = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01
        };

        testFile = new MockMultipartFile(
                "file",
                "test-photo.jpg",
                "image/jpeg",
                jpegBytes
        );
    }

    @Test
    void testUploadPhoto_AsOwner_Success() throws IOException {
        // Arrange
        Map<String, Object> cloudinaryResponse = new HashMap<>();
        cloudinaryResponse.put("public_id", "flight-photos/abc123");
        cloudinaryResponse.put("secure_url", "https://res.cloudinary.com/test/image/upload/v123/abc123.jpg");

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(cloudinaryService.upload(any(MultipartFile.class))).thenReturn(cloudinaryResponse);
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> {
            Photo photo = invocation.getArgument(0);
            photo.setId(1L);
            return photo;
        });

        // Act
        Photo result = photoService.uploadPhoto(testFile, 1L, testUserId, false);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getOwnerUserId());
        assertEquals("flight-photos/abc123", result.getCloudinaryPublicId());
        assertTrue(result.getSecureUrl().contains("cloudinary.com"));
        
        verify(cloudinaryService, times(1)).upload(testFile);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void testUploadPhoto_AsAdmin_Success() throws IOException {
        // Arrange
        Map<String, Object> cloudinaryResponse = new HashMap<>();
        cloudinaryResponse.put("public_id", "flight-photos/admin123");
        cloudinaryResponse.put("secure_url", "https://res.cloudinary.com/test/image/upload/v123/admin123.jpg");

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(cloudinaryService.upload(any(MultipartFile.class))).thenReturn(cloudinaryResponse);
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> {
            Photo photo = invocation.getArgument(0);
            photo.setId(2L);
            return photo;
        });

        // Act - Admin uploading to someone else's sighting
        Photo result = photoService.uploadPhoto(testFile, 1L, "admin_user", true);

        // Assert
        assertNotNull(result);
        verify(cloudinaryService, times(1)).upload(testFile);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void testUploadPhoto_AsNonOwner_ThrowsException() throws IOException {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            photoService.uploadPhoto(testFile, 1L, "different_user", false);
        });

        verify(cloudinaryService, never()).upload(any());
        verify(photoRepository, never()).save(any());
    }

    @Test
    void testUploadPhoto_SightingNotFound_ThrowsException() throws IOException {
        // Arrange
        when(sightingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            photoService.uploadPhoto(testFile, 999L, testUserId, false);
        });

        verify(cloudinaryService, never()).upload(any());
    }

    @Test
    void testUploadPhoto_CloudinaryFails_ThrowsException() throws IOException {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(cloudinaryService.upload(any(MultipartFile.class)))
                .thenThrow(new IOException("Cloudinary error"));

        // Act & Assert
        assertThrows(IOException.class, () -> {
            photoService.uploadPhoto(testFile, 1L, testUserId, false);
        });

        verify(photoRepository, never()).save(any());
    }

    @Test
    void testDeletePhoto_AsOwner_Success() throws IOException {
        // Arrange
        Photo photo = Photo.builder()
                .id(1L)
                .sighting(testSighting)
                .ownerUserId(testUserId)
                .cloudinaryPublicId("flight-photos/abc123")
                .secureUrl("https://res.cloudinary.com/test/abc123.jpg")
                .build();

        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        doNothing().when(cloudinaryService).delete(anyString());

        // Act
        photoService.deletePhoto(1L, testUserId, false);

        // Assert
        verify(cloudinaryService, times(1)).delete("flight-photos/abc123");
        verify(photoRepository, times(1)).delete(photo);
    }

    @Test
    void testDeletePhoto_AsAdmin_Success() throws IOException {
        // Arrange
        Photo photo = Photo.builder()
                .id(1L)
                .sighting(testSighting)
                .ownerUserId(testUserId)
                .cloudinaryPublicId("flight-photos/xyz789")
                .secureUrl("https://res.cloudinary.com/test/xyz789.jpg")
                .build();

        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));
        doNothing().when(cloudinaryService).delete(anyString());

        // Act
        photoService.deletePhoto(1L, "admin_user", true);

        // Assert
        verify(cloudinaryService, times(1)).delete("flight-photos/xyz789");
        verify(photoRepository, times(1)).delete(photo);
    }

    @Test
    void testDeletePhoto_AsNonOwner_ThrowsException() throws IOException {
        // Arrange
        Photo photo = Photo.builder()
                .id(1L)
                .sighting(testSighting)
                .ownerUserId(testUserId)
                .cloudinaryPublicId("flight-photos/abc123")
                .build();

        when(photoRepository.findById(1L)).thenReturn(Optional.of(photo));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            photoService.deletePhoto(1L, "different_user", false);
        });

        verify(cloudinaryService, never()).delete(anyString());
        verify(photoRepository, never()).delete(any());
    }

    @Test
    void testDeletePhoto_PhotoNotFound_ThrowsException() {
        // Arrange
        when(photoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            photoService.deletePhoto(999L, testUserId, false);
        });
    }

    @Test
    void testUploadPhoto_ExtractsCloudinaryPublicId() throws IOException {
        // Arrange
        Map<String, Object> cloudinaryResponse = new HashMap<>();
        cloudinaryResponse.put("public_id", "flight-photos/2024/december/photo-abc123");
        cloudinaryResponse.put("secure_url", "https://res.cloudinary.com/test/image/upload/v123/photo.jpg");

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(cloudinaryService.upload(any(MultipartFile.class))).thenReturn(cloudinaryResponse);
        when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> {
            Photo photo = invocation.getArgument(0);
            photo.setId(1L);
            return photo;
        });

        // Act
        Photo result = photoService.uploadPhoto(testFile, 1L, testUserId, false);

        // Assert
        assertEquals("flight-photos/2024/december/photo-abc123", result.getCloudinaryPublicId());
        assertTrue(result.getSecureUrl().contains("cloudinary.com"));
    }
}
