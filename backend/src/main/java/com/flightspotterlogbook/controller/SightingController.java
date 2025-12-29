package com.flightspotterlogbook.controller;

import com.flightspotterlogbook.dto.SightingRequest;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.service.SightingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing sightings.
 */
@RestController
@RequestMapping("/api/sightings")
@RequiredArgsConstructor
public class SightingController {

    private final SightingService sightingService;

    /**
     * Returns the current user's sightings. Pagination parameters are optional.
     * Maximum page size is enforced at 100 to prevent excessive data transfer.
     */
    @GetMapping("/me")
    public Page<Sighting> mySightings(Authentication authentication,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        // Enforce maximum page size
        if (size > 100) {
            size = 100;
        }
        String userId = authentication.getName();
        return sightingService.getSightingsForUser(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    /**
     * Returns public sightings. Pagination parameters are optional.
     * Maximum page size is enforced at 100 to prevent excessive data transfer.
     */
    @GetMapping
    public Page<Sighting> publicSightings(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        // Enforce maximum page size
        if (size > 100) {
            size = 100;
        }
        return sightingService.getPublicSightings(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    /**
     * Returns ALL sightings for admin users (public and private).
     * Only accessible by users with ROLE_ADMIN.
     */
    @GetMapping("/admin/all")
    public Page<Sighting> allSightings(Authentication authentication,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size) {
        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            throw new IllegalStateException("Access denied. Admin role required.");
        }
        
        // Enforce maximum page size
        if (size > 100) {
            size = 100;
        }
        
        return sightingService.getAllSightings(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp")));
    }

    /**
     * Returns a single sighting by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Sighting> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sightingService.getById(id));
    }

    /**
     * Creates a new sighting for the authenticated user. The request body must include
     * all required fields.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sighting create(@Valid @RequestBody SightingRequest request, Authentication authentication) {
        Sighting sighting = Sighting.builder()
                .timestamp(request.getTimestamp())
                .airportIataOrIcao(request.getAirportIataOrIcao())
                .locationText(request.getLocationText())
                .airline(request.getAirline())
                .callsign(request.getCallsign())
                .icao24(request.getIcao24())
                .registration(request.getRegistration())
                .aircraftModel(request.getAircraftModel())
                .notes(request.getNotes())
                .visibility(request.getVisibility())
                .build();
        return sightingService.createSighting(sighting, authentication.getName());
    }

    /**
     * Updates a sighting. Only the owner or an admin can update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Sighting> update(@PathVariable Long id,
                                           @Valid @RequestBody SightingRequest request,
                                           Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        Sighting incoming = Sighting.builder()
                .timestamp(request.getTimestamp())
                .airportIataOrIcao(request.getAirportIataOrIcao())
                .locationText(request.getLocationText())
                .airline(request.getAirline())
                .callsign(request.getCallsign())
                .icao24(request.getIcao24())
                .registration(request.getRegistration())
                .aircraftModel(request.getAircraftModel())
                .notes(request.getNotes())
                .visibility(request.getVisibility())
                .build();
        Sighting updated = sightingService.updateSighting(id, incoming, authentication.getName(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a sighting. Only the owner or an admin can delete.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        sightingService.deleteSighting(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retry enrichment for a sighting. Only the owner or an admin can retry.
     */
    @PostMapping("/{id}/enrich")
    public ResponseEntity<Void> retryEnrichment(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        sightingService.retryEnrichment(id, authentication.getName(), isAdmin);
        return ResponseEntity.accepted().build();
    }
}