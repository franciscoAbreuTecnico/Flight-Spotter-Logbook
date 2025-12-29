package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.EnrichmentStatus;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.Visibility;
import com.flightspotterlogbook.repository.SightingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service layer for managing sightings. Handles business logic such as assigning the owner
 * and initiating enrichment jobs.
 */
@Service
@RequiredArgsConstructor
public class SightingService {

    private final SightingRepository sightingRepository;
    private final OpenSkyService openSkyService;

    /**
     * Creates a new sighting for the given user. The sighting is persisted and an enrichment
     * job is started asynchronously.
     *
     * @param sighting the sighting to persist
     * @param userId the ID of the authenticated user
     * @return the persisted sighting
     */
    @Transactional
    public Sighting createSighting(Sighting sighting, String userId) {
        sighting.setOwnerUserId(userId);
        sighting.setEnrichmentStatus(EnrichmentStatus.ENRICHING);
        sighting.setCreatedAt(LocalDateTime.now());
        sighting.setUpdatedAt(sighting.getCreatedAt());
        Sighting saved = sightingRepository.save(sighting);
        openSkyService.enrichAsync(saved);
        return saved;
    }

    /**
     * Updates an existing sighting. Only the owner or an admin should call this method. Fields
     * that are null on the incoming sighting are ignored.
     */
    @Transactional
    public Sighting updateSighting(Long id, Sighting incoming, String userId, boolean isAdmin) {
        Sighting existing = sightingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
        if (!isAdmin && !existing.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("Not authorised to update this sighting");
        }
        // Only overwrite fields that are not null on incoming
        if (incoming.getTimestamp() != null) existing.setTimestamp(incoming.getTimestamp());
        if (incoming.getAirportIataOrIcao() != null) existing.setAirportIataOrIcao(incoming.getAirportIataOrIcao());
        if (incoming.getLocationText() != null) existing.setLocationText(incoming.getLocationText());
        if (incoming.getAirline() != null) existing.setAirline(incoming.getAirline());
        if (incoming.getCallsign() != null) existing.setCallsign(incoming.getCallsign());
        if (incoming.getIcao24() != null) existing.setIcao24(incoming.getIcao24());
        if (incoming.getRegistration() != null) existing.setRegistration(incoming.getRegistration());
        if (incoming.getAircraftModel() != null) existing.setAircraftModel(incoming.getAircraftModel());
        if (incoming.getNotes() != null) existing.setNotes(incoming.getNotes());
        if (incoming.getVisibility() != null) existing.setVisibility(incoming.getVisibility());
        existing.setUpdatedAt(LocalDateTime.now());
        return sightingRepository.save(existing);
    }

    /**
     * Deletes a sighting by ID. Only the owner or an admin can delete.
     */
    @Transactional
    public void deleteSighting(Long id, String userId, boolean isAdmin) {
        Sighting existing = sightingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
        if (!isAdmin && !existing.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("Not authorised to delete this sighting");
        }
        sightingRepository.delete(existing);
    }

    /**
     * Retries enrichment for a sighting. Only the owner or an admin can retry.
     */
    @Transactional
    public void retryEnrichment(Long id, String userId, boolean isAdmin) {
        Sighting existing = sightingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
        if (!isAdmin && !existing.getOwnerUserId().equals(userId)) {
            throw new IllegalStateException("Not authorised to retry enrichment for this sighting");
        }
        existing.setEnrichmentStatus(EnrichmentStatus.ENRICHING);
        existing.setUpdatedAt(LocalDateTime.now());
        sightingRepository.save(existing);
        openSkyService.enrichAsync(existing);
    }

    /**
     * Returns a page of sightings created by the given user.
     */
    @Transactional(readOnly = true)
    public Page<Sighting> getSightingsForUser(String userId, Pageable pageable) {
        return sightingRepository.findByOwnerUserId(userId, pageable);
    }

    /**
     * Returns a page of public sightings.
     */
    @Transactional(readOnly = true)
    public Page<Sighting> getPublicSightings(Pageable pageable) {
        return sightingRepository.findByVisibility(Visibility.PUBLIC, pageable);
    }

    /**
     * Returns ALL sightings (public and private) for admin purposes.
     */
    @Transactional(readOnly = true)
    public Page<Sighting> getAllSightings(Pageable pageable) {
        return sightingRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Sighting getById(Long id) {
        return sightingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sighting not found"));
    }
}