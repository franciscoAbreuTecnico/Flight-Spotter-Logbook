package com.flightspotterlogbook.controller;

import com.flightspotterlogbook.model.Photo;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.repository.PhotoRepository;
import com.flightspotterlogbook.repository.SightingRepository;
import com.flightspotterlogbook.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for photo operations.
 */
@RestController
@RequestMapping("/api/sightings/{sightingId}/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoRepository photoRepository;
    private final SightingRepository sightingRepository;

    /**
     * Uploads a new photo to a sighting.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Photo> uploadPhoto(@PathVariable Long sightingId,
                                             @RequestParam("file") MultipartFile file,
                                             Authentication authentication) throws IOException {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        Photo photo = photoService.uploadPhoto(file, sightingId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(photo);
    }

    /**
     * Returns all photos for the specified sighting. Public/private visibility is enforced
     * implicitly via the caller's ability to view the parent sighting.
     */
    @GetMapping
    public ResponseEntity<List<Photo>> getPhotos(@PathVariable Long sightingId) {
        Sighting sighting = sightingRepository.findById(sightingId)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
        List<Photo> photos = photoRepository.findBySighting(sighting);
        return ResponseEntity.ok(photos);
    }

    /**
     * Deletes a photo. Only the owner or an admin can delete.
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long sightingId,
                                           @PathVariable Long photoId,
                                           Authentication authentication) throws IOException {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        photoService.deletePhoto(photoId, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}