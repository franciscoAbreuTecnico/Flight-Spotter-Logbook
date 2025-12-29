package com.flightspotterlogbook.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service encapsulating interactions with Cloudinary. Uses signed uploads to store images and
 * returns metadata about the uploaded file. Delete operations are not yet implemented but
 * can be added later.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        cloudinary = new Cloudinary(config);
    }

    /**
     * Uploads a file to Cloudinary with security validations.
     * Validates file size, type, and content before upload.
     *
     * @param file multipart file from the client
     * @return map with upload result
     * @throws IOException if the upload fails
     * @throws IllegalArgumentException if file validation fails
     */
    public Map<String, Object> upload(MultipartFile file) throws IOException {
        // Security validations
        validateFile(file);
        
        Map<String, Object> options = new HashMap<>();
        // Force resource_type to image for additional Cloudinary validation
        options.put("resource_type", "image");
        // Organize uploads in folders
        options.put("folder", "flight-spotter-photos");
        // Auto-optimize images (format, quality)
        options.put("quality", "auto:good");
        options.put("fetch_format", "auto");
        // Generate thumbnails automatically using Transformation objects
        options.put("eager", List.of(
            new Transformation<>().width(800).height(600).crop("limit").quality("auto:good"),
            new Transformation<>().width(300).height(300).crop("thumb").gravity("auto")
        ));
        
        Map<?, ?> rawResult = cloudinary.uploader().upload(file.getBytes(), options);
        
        Map<String, Object> uploadResult = new HashMap<>();
        for (Map.Entry<?, ?> entry : rawResult.entrySet()) {
            Object keyObj = entry.getKey();
            if (keyObj instanceof String) {
                String key = (String) keyObj;
                uploadResult.put(key, entry.getValue());
            }
        }
        
        log.info("Successfully uploaded file to Cloudinary (size: {} bytes, type: {}, public_id: {})", 
                file.getSize(), file.getContentType(), uploadResult.get("public_id"));
        return uploadResult;
    }

    /**
     * Validates uploaded file for security issues.
     * Checks: size, content type, filename, and magic bytes.
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        // 1. File size validation (max 25MB)
        long maxFileSize = 25 * 1024 * 1024; // 25MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed (25MB)");
        }

        // 2. Content type validation (only images)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // 3. Allowed MIME types
        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isAllowed = false;
        for (String allowed : allowedTypes) {
            if (allowed.equalsIgnoreCase(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            throw new IllegalArgumentException("File type not allowed. Supported: JPEG, PNG, GIF, WebP");
        }

        // 4. Filename validation (prevent path traversal)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..") || 
            originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        // 5. Magic bytes validation (verify actual file type)
        byte[] bytes = file.getBytes();
        if (bytes.length < 8) {
            throw new IllegalArgumentException("File is too small to be a valid image");
        }

        // Check magic bytes for common image formats
        if (!isValidImageMagicBytes(bytes)) {
            throw new IllegalArgumentException("File does not appear to be a valid image");
        }

        log.debug("File validation passed: {} ({})", originalFilename, contentType);
    }

    /**
     * Validates file magic bytes to verify actual file type.
     * Prevents content-type spoofing attacks.
     */
    private boolean isValidImageMagicBytes(byte[] bytes) {
        // JPEG: FF D8 FF
        if (bytes.length >= 3 && 
            (bytes[0] & 0xFF) == 0xFF && 
            (bytes[1] & 0xFF) == 0xD8 && 
            (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (bytes.length >= 8 &&
            (bytes[0] & 0xFF) == 0x89 &&
            (bytes[1] & 0xFF) == 0x50 &&
            (bytes[2] & 0xFF) == 0x4E &&
            (bytes[3] & 0xFF) == 0x47) {
            return true;
        }

        // GIF: 47 49 46 38 (GIF8)
        if (bytes.length >= 4 &&
            (bytes[0] & 0xFF) == 0x47 &&
            (bytes[1] & 0xFF) == 0x49 &&
            (bytes[2] & 0xFF) == 0x46 &&
            (bytes[3] & 0xFF) == 0x38) {
            return true;
        }

        // WebP: 52 49 46 46 ... 57 45 42 50 (RIFF....WEBP)
        if (bytes.length >= 12 &&
            (bytes[0] & 0xFF) == 0x52 &&
            (bytes[1] & 0xFF) == 0x49 &&
            (bytes[2] & 0xFF) == 0x46 &&
            (bytes[3] & 0xFF) == 0x46 &&
            (bytes[8] & 0xFF) == 0x57 &&
            (bytes[9] & 0xFF) == 0x45 &&
            (bytes[10] & 0xFF) == 0x42 &&
            (bytes[11] & 0xFF) == 0x50) {
            return true;
        }

        return false;
    }

    /**
     * Deletes an image from Cloudinary.
     *
     * @param publicId the Cloudinary public ID of the image
     * @throws IOException if deletion fails
     */
    public void delete(String publicId) throws IOException {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, Map.of());
            String resultStatus = result.get("result").toString();
            
            if ("ok".equals(resultStatus) || "not found".equals(resultStatus)) {
                log.info("Successfully deleted image from Cloudinary: {}", publicId);
            } else {
                log.warn("Unexpected result when deleting image {}: {}", publicId, resultStatus);
            }
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
            throw e;
        }
    }
}