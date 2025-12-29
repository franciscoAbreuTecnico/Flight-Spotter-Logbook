package com.flightspotterlogbook.dto;

import com.flightspotterlogbook.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data transfer object used when creating or updating a sighting.
 */
@Data
public class SightingRequest {
    @NotNull
    private LocalDateTime timestamp;

    @NotBlank
    private String airportIataOrIcao;

    private String locationText;
    private String airline;
    private String callsign;
    private String icao24;
    private String registration;
    private String aircraftModel;
    private String notes;
    private Visibility visibility = Visibility.PUBLIC;
}