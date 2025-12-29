package com.flightspotterlogbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing an aircraft from OpenSky.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AircraftDTO {
    private String icao24;
    private String callsign;
    private String registration;
    private String model;
    private String manufacturer;
    private String operator;
    private String originCountry;
}
