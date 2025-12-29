package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.Photo;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.repository.PhotoRepository;
import com.flightspotterlogbook.repository.SightingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Handles uploading photos to Cloudinary and persisting metadata in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final SightingRepository sightingRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Uploads a photo for a given sighting. The user must be the owner of the sighting or an admin.
     *
     * @param file the uploaded file
     * @param sightingId the ID of the sighting
     * @param userId ID of the authenticated user
     * @param isAdmin whether the user has admin privileges
     * @return the created Photo entity
     */
    @Transactional
    public Photo uploadPhoto(MultipartFile file, Long sightingId, String userId, boolean isAdmin) throws IOException {
        Sighting sighting = sightingRepository.findById(sightingId)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
        if (!isAdmin && !sighting.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("Not authorised to upload photo for this sighting");
        }
        Map<String, Object> result = cloudinaryService.upload(file);
        String publicId = result.get("public_id").toString();
        String secureUrl = result.get("secure_url").toString();
        Photo photo = Photo.builder()
                .sighting(sighting)
                .ownerUserId(userId)
                .cloudinaryPublicId(publicId)
                .secureUrl(secureUrl)
                .build();
        return photoRepository.save(photo);
    }

    /**
     * Deletes a photo from both Cloudinary and database.
     * Only the owner or an admin can delete.
     *
     * @param photoId the ID of the photo to delete
     * @param userId ID of the authenticated user
     * @param isAdmin whether the user has admin privileges
     */
    @Transactional
    public void deletePhoto(Long photoId, String userId, boolean isAdmin) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));
        
        if (!isAdmin && !photo.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("Not authorised to delete this photo");
        }
        
        // Delete from Cloudinary first
        cloudinaryService.delete(photo.getCloudinaryPublicId());
        
        // Then delete from database
        photoRepository.delete(photo);
        
        log.info("Deleted photo {} by user {}", photoId, userId);
    }
}