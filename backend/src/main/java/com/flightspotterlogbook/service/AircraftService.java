package com.flightspotterlogbook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspotterlogbook.dto.AircraftDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching live aircraft data from OpenSky Network API.
 * Uses the new OpenSky Trino API with OAuth2 client credentials.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AircraftService {

    @Value("${opensky.client-id:}")
    private String clientId;

    @Value("${opensky.client-secret:}")
    private String clientSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // European bounding box: SW corner to NE corner
    // Covers from Portugal/Iceland to Finland/Turkey
    private static final double LAT_MIN = 34.0;  // Southern tip of Cyprus/Crete
    private static final double LAT_MAX = 71.0;  // Northern Norway
    private static final double LON_MIN = -25.0; // Azores/Iceland
    private static final double LON_MAX = 45.0;  // Eastern Turkey/Finland

    /**
     * Search for aircraft near a specific airport (within ~50km radius).
     * Uses a smaller bounding box to reduce API credit usage (1 credit per request).
     * 
     * @param airportLat Airport latitude
     * @param airportLon Airport longitude
     * @param query Optional query to filter results by callsign/icao24
     * @return List of aircraft near the airport
     */
    public List<AircraftDTO> searchAircraftNearAirport(double airportLat, double airportLon, String query) {
        try {
            // Create ~50km radius bounding box (approximately 0.5 degrees)
            double delta = 0.5;
            double lamin = airportLat - delta;
            double lamax = airportLat + delta;
            double lomin = airportLon - delta;
            double lomax = airportLon + delta;
            
            String url = String.format(
                "https://opensky-network.org/api/states/all?lamin=%.2f&lomin=%.2f&lamax=%.2f&lomax=%.2f",
                lamin, lomin, lamax, lomax
            );

            log.debug("Fetching aircraft near airport at {},{} (box: {},{} to {},{})", 
                airportLat, airportLon, lamin, lomin, lamax, lomax);

            // Use anonymous access - small bounding box uses only 1 API credit
            WebClient webClient = WebClient.builder().build();

            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(e -> {
                        log.error("Error fetching aircraft near airport: {}", e.getMessage());
                        return Mono.just("{\"states\":[]}");
                    })
                    .block();

            return parseAircraftResponse(response, query);
        } catch (Exception e) {
            log.error("Failed to fetch aircraft near airport", e);
            return List.of();
        }
    }

    /**
     * Search for currently flying aircraft over Europe.
     * Uses the OpenSky states/all endpoint filtered to European airspace.
     * Note: Uses 4 API credits per request due to large area.
     */
    public List<AircraftDTO> searchAircraftOverEurope(String query) {
        try {
            String url = String.format(
                "https://opensky-network.org/api/states/all?lamin=%.1f&lomin=%.1f&lamax=%.1f&lomax=%.1f",
                LAT_MIN, LON_MIN, LAT_MAX, LON_MAX
            );

            // Use anonymous access (no authentication)
            // OpenSky's API has rate limits for anonymous users but doesn't require auth
            WebClient webClient = WebClient.builder().build();

            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(15))
                    .onErrorResume(e -> {
                        log.error("Error fetching aircraft from OpenSky: {}", e.getMessage());
                        return Mono.just("{\"states\":[]}");
                    })
                    .block();

            return parseAircraftResponse(response, query);
        } catch (Exception e) {
            log.error("Failed to fetch aircraft data", e);
            return List.of();
        }
    }

    /**
     * Parse the OpenSky API response and filter by query.
     */
    private List<AircraftDTO> parseAircraftResponse(String response, String query) {
        List<AircraftDTO> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode states = root.get("states");
            
            if (states == null || !states.isArray()) {
                return result;
            }

            String lowerQuery = query != null ? query.toLowerCase().trim() : "";

            for (JsonNode state : states) {
                if (!state.isArray() || state.size() < 8) continue;

                String icao24 = getTextValue(state.get(0));
                String callsign = getTextValue(state.get(1));
                String originCountry = getTextValue(state.get(2));

                // Skip if no callsign (not useful for users)
                if (callsign == null || callsign.isBlank()) continue;
                
                callsign = callsign.trim();

                // Filter by query if provided
                if (!lowerQuery.isEmpty()) {
                    boolean matches = (icao24 != null && icao24.toLowerCase().contains(lowerQuery)) ||
                                     callsign.toLowerCase().contains(lowerQuery) ||
                                     (originCountry != null && originCountry.toLowerCase().contains(lowerQuery));
                    if (!matches) continue;
                }

                // Fetch metadata for this aircraft (registration, model, operator)
                AircraftDTO aircraft = AircraftDTO.builder()
                        .icao24(icao24)
                        .callsign(callsign)
                        .originCountry(originCountry)
                        .build();
                
                // Enrich with metadata from OpenSky database
                enrichAircraftMetadata(aircraft);
                
                result.add(aircraft);

                // Limit results
                if (result.size() >= 50) break;
            }
        } catch (Exception e) {
            log.error("Error parsing aircraft response", e);
        }
        return result;
    }

    private String getTextValue(JsonNode node) {
        if (node == null || node.isNull()) return null;
        return node.asText();
    }

    /**
     * Enrich aircraft with metadata (registration, model, operator) from OpenSky database.
     * This data is cached by OpenSky and doesn't count against API credits.
     */
    private void enrichAircraftMetadata(AircraftDTO aircraft) {
        if (aircraft.getIcao24() == null) return;
        
        try {
            String url = "https://opensky-network.org/api/metadata/aircraft/icao/" + 
                         aircraft.getIcao24().toLowerCase();
            
            WebClient webClient = WebClient.builder().build();
            
            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorResume(e -> {
                        log.debug("No metadata for aircraft {}: {}", aircraft.getIcao24(), e.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            if (response != null && !response.isBlank()) {
                JsonNode metadata = objectMapper.readTree(response);
                
                // Extract metadata fields
                aircraft.setRegistration(getTextValue(metadata.get("registration")));
                aircraft.setModel(getTextValue(metadata.get("model")));
                aircraft.setManufacturer(getTextValue(metadata.get("manufacturername")));
                aircraft.setOperator(getTextValue(metadata.get("operator")));
                
                log.debug("Enriched aircraft {}: {} {} operated by {}", 
                    aircraft.getIcao24(), 
                    aircraft.getManufacturer(), 
                    aircraft.getModel(), 
                    aircraft.getOperator());
            }
        } catch (Exception e) {
            // Metadata not critical - just log and continue
            log.debug("Failed to fetch metadata for aircraft {}", aircraft.getIcao24());
        }
    }

    /**
     * Get aircraft by ICAO24 hex code.
     */
    public AircraftDTO getAircraftByIcao24(String icao24) {
        try {
            String url = "https://opensky-network.org/api/states/all?icao24=" + icao24.toLowerCase();

            // Use anonymous access
            WebClient webClient = WebClient.builder().build();

            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(e -> Mono.just("{\"states\":[]}"))
                    .block();

            List<AircraftDTO> aircraft = parseAircraftResponse(response, null);
            return aircraft.isEmpty() ? null : aircraft.get(0);
        } catch (Exception e) {
            log.error("Failed to get aircraft by ICAO24: {}", icao24, e);
            return null;
        }
    }
}
