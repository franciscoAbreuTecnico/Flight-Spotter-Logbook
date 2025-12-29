package com.flightspotterlogbook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing an airport.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirportDTO {
    private String icao;
    private String iata;
    private String name;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
}
