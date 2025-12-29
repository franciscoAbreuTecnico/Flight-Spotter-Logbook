package com.flightspotterlogbook.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Persists cached responses from the OpenSky API. Each entry is keyed by a hash of the query
 * parameters and has an expiry timestamp. Using a database cache allows the enrichment service
 * to avoid repeated calls to OpenSky and respect rate limits.
 */
@Entity
@Table(name = "opensky_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenSkyCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hash representing the query parameters used to call the OpenSky API. Should be unique.
     */
    @Column(name = "query_hash", nullable = false, unique = true)
    private String queryHash;

    /**
     * The raw JSON response from OpenSky. Stored as a string.
     */
    @Column(name = "response", nullable = false, columnDefinition = "TEXT")
    private String response;

    /**
     * When this cache entry expires and should be evicted. UTC timestamp.
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}