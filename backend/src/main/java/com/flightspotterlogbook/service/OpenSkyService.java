package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.EnrichmentStatus;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.OpenSkyCache;
import com.flightspotterlogbook.repository.OpenSkyCacheRepository;
import com.flightspotterlogbook.repository.SightingRepository;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service responsible for enriching sightings with data from the OpenSky Network. Requests are
 * performed asynchronously to avoid blocking the main request/response thread. Responses are
 * cached both in memory (via Caffeine) and in the database to minimise calls to the external
 * API and respect rate limits.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSkyService {

    private final OpenSkyCacheRepository cacheRepository;
    private final SightingRepository sightingRepository;
    private final WebClient openSkyWebClient;
    
    @Qualifier("openSkyRateLimiter")
    private final Bucket openSkyRateLimiter;

    private final Duration defaultTtl = Duration.ofHours(24);  // Increased from 1 hour to 24 hours

    /**
     * Initiates an asynchronous enrichment for the given sighting. Once the enrichment
     * completes, the sighting is updated and persisted.
     *
     * @param sighting the sighting to enrich
     */
    @Async
    public void enrichAsync(Sighting sighting) {
        try {
            log.debug("Starting enrichment for sighting {}", sighting.getId());
            String query = buildQueryForSighting(sighting);
            String hash = computeHash(query);
            Optional<OpenSkyCache> cached = cacheRepository.findByQueryHash(hash);
            
            String response;
            
            // First, check if we have valid (non-expired) cache
            if (cached.isPresent() && cached.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                log.debug("Using valid cached OpenSky data for sighting {}", sighting.getId());
                response = cached.get().getResponse();
            } 
            // If cache is expired or missing, try to fetch from OpenSky API
            else {
                // Check rate limit before making API call
                if (!openSkyRateLimiter.tryConsume(1)) {
                    log.warn("OpenSky rate limit exceeded. Tokens remaining: {}", 
                            openSkyRateLimiter.getAvailableTokens());
                    
                    // Graceful degradation: use expired cache if available
                    if (cached.isPresent()) {
                        log.info("Using expired cache due to rate limit for sighting {}", sighting.getId());
                        response = cached.get().getResponse();
                    } else {
                        // No cache available, mark as failed
                        log.error("No cached data available and rate limit exceeded for sighting {}", 
                                sighting.getId());
                        sighting.setEnrichmentStatus(EnrichmentStatus.FAILED);
                        sightingRepository.save(sighting);
                        return;
                    }
                } else {
                    // Rate limit OK, fetch from API
                    log.debug("Fetching from OpenSky API. Tokens remaining: {}", 
                            openSkyRateLimiter.getAvailableTokens());
                    response = openSkyWebClient.get()
                            .uri(query)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    
                    // Save to cache
                    cacheRepository.save(OpenSkyCache.builder()
                            .queryHash(hash)
                            .response(response)
                            .expiresAt(LocalDateTime.now().plus(defaultTtl))
                            .build());
                    log.debug("Cached OpenSky response for sighting {} (TTL: {})", 
                            sighting.getId(), defaultTtl);
                }
            }

            // TODO: parse response JSON and set registration/aircraftModel etc.
            // For this MVP we simply mark the sighting as enriched.
            log.info("OpenSky response for sighting {}: {}", sighting.getId(), 
                    response != null && response.length() > 100 ? response.substring(0, 100) + "..." : response);
            sighting.setEnrichmentStatus(EnrichmentStatus.ENRICHED);
            sightingRepository.save(sighting);
            log.debug("Successfully enriched sighting {}", sighting.getId());
        } catch (Exception ex) {
            log.error("Failed to enrich sighting {}", sighting.getId(), ex);
            sighting.setEnrichmentStatus(EnrichmentStatus.FAILED);
            sightingRepository.save(sighting);
        }
    }

    /**
     * Builds the OpenSky query string for the given sighting. If the sighting contains a callsign
     * and timestamp, that information is used; otherwise, icao24 is used if present. In the
     * absence of both, an empty query is returned which will likely fail but not crash the app.
     */
    private String buildQueryForSighting(Sighting sighting) {
        // Example endpoint: /states/all?icao24=abc123&time=UNIX_EPOCH_SECONDS
        if (sighting.getIcao24() != null && !sighting.getIcao24().isBlank()) {
            long epochSeconds = sighting.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC);
            return "/states/all?icao24=" + sighting.getIcao24() + "&time=" + epochSeconds;
        }
        if (sighting.getCallsign() != null && !sighting.getCallsign().isBlank()) {
            // There is no direct endpoint for callsign; this is placeholder for actual implementation.
            return "/states/all?callsign=" + sighting.getCallsign().trim();
        }
        // Fallback: return root path to avoid errors
        return "/";
    }

    /**
     * Computes a simple MD5 hash of the query string for caching.
     */
    private String computeHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}