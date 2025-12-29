package com.flightspotterlogbook.controller;

import com.flightspotterlogbook.dto.AircraftDTO;
import com.flightspotterlogbook.dto.AirportDTO;
import com.flightspotterlogbook.service.AircraftService;
import com.flightspotterlogbook.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for lookup endpoints (airports and aircraft).
 * These endpoints are public to allow autocomplete functionality.
 */
@RestController
@RequestMapping("/api/lookup")
@RequiredArgsConstructor
public class LookupController {

    private final AirportService airportService;
    private final AircraftService aircraftService;

    /**
     * Search European airports by query string.
     * Matches against ICAO, IATA, name, city, or country.
     * 
     * @param q search query (minimum 2 characters)
     * @return list of matching airports (max 20)
     */
    @GetMapping("/airports")
    public ResponseEntity<List<AirportDTO>> searchAirports(@RequestParam(defaultValue = "") String q) {
        List<AirportDTO> airports = airportService.searchAirports(q);
        return ResponseEntity.ok(airports);
    }

    /**
     * Get all European airports.
     * 
     * @return list of all airports
     */
    @GetMapping("/airports/all")
    public ResponseEntity<List<AirportDTO>> getAllAirports() {
        return ResponseEntity.ok(airportService.getAllAirports());
    }

    /**
     * Get airport by ICAO or IATA code.
     * 
     * @param code ICAO or IATA code
     * @return airport details or 404 if not found
     */
    @GetMapping("/airports/{code}")
    public ResponseEntity<AirportDTO> getAirportByCode(@PathVariable String code) {
        AirportDTO airport = airportService.getAirportByCode(code);
        if (airport == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(airport);
    }

    /**
     * Search for live aircraft currently flying near a specific airport or over Europe.
     * Uses OpenSky Network API to fetch real-time data.
     * 
     * If airport coordinates are provided, searches within ~50km radius (uses 1 API credit).
     * Otherwise searches all of Europe (uses 4 API credits).
     * 
     * @param q optional search query to filter by callsign, ICAO24, or country
     * @param lat optional airport latitude for localized search
     * @param lon optional airport longitude for localized search
     * @return list of aircraft currently in the specified airspace
     */
    @GetMapping("/aircraft")
    public ResponseEntity<List<AircraftDTO>> searchAircraft(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {
        
        List<AircraftDTO> aircraft;
        
        // If airport coordinates provided, search near that airport (saves API credits)
        if (lat != null && lon != null) {
            aircraft = aircraftService.searchAircraftNearAirport(lat, lon, q);
        } else {
            // Otherwise search all of Europe
            aircraft = aircraftService.searchAircraftOverEurope(q);
        }
        
        return ResponseEntity.ok(aircraft);
    }

    /**
     * Get aircraft by ICAO24 hex code.
     * 
     * @param icao24 ICAO24 hex identifier
     * @return aircraft details or 404 if not found
     */
    @GetMapping("/aircraft/{icao24}")
    public ResponseEntity<AircraftDTO> getAircraftByIcao24(@PathVariable String icao24) {
        AircraftDTO aircraft = aircraftService.getAircraftByIcao24(icao24);
        if (aircraft == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(aircraft);
    }
}
