package com.flightspotterlogbook.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA entity representing a single aircraft sighting.
 */
@Entity
@Table(name = "sightings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sighting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier of the user who created the sighting, typically the Clerk user ID.
     */
    @Column(name = "owner_user_id", nullable = false)
    private String ownerUserId;

    /**
     * Timestamp when the sighting occurred.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Airport code (IATA or ICAO) associated with the sighting.
     */
    @Column(name = "airport_iata_or_icao", nullable = false)
    private String airportIataOrIcao;

    /**
     * Free‑form location description.
     */
    private String locationText;

    private String airline;
    private String callsign;
    private String icao24;
    private String registration;
    private String aircraftModel;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrichment_status", nullable = false)
    private EnrichmentStatus enrichmentStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.enrichmentStatus == null) {
            this.enrichmentStatus = EnrichmentStatus.ENRICHING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}