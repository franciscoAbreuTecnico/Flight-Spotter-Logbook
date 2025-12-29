package com.flightspotterlogbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightspotterlogbook.dto.SightingRequest;
import com.flightspotterlogbook.model.EnrichmentStatus;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.Visibility;
import com.flightspotterlogbook.service.SightingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SightingController.
 * Tests REST endpoints with Spring Security context.
 */
@WebMvcTest(SightingController.class)
@AutoConfigureMockMvc(addFilters = false)
class SightingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SightingService sightingService;

    private Sighting testSighting;

    @BeforeEach
    void setUp() {
        testSighting = Sighting.builder()
                .id(1L)
                .ownerUserId("user_123")
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("LAX")
                .locationText("Los Angeles, USA")
                .airline("American Airlines")
                .callsign("AAL123")
                .visibility(Visibility.PUBLIC)
                .enrichmentStatus(EnrichmentStatus.ENRICHED)
                .build();
    }

    @Test
    @WithAnonymousUser
    void testGetPublicSightings_NoAuth_ReturnsPublicOnly() throws Exception {
        // Arrange
        List<Sighting> sightings = Arrays.asList(testSighting);
        Page<Sighting> page = new PageImpl<>(sightings);
        
        when(sightingService.getPublicSightings(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/sightings")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"));

        verify(sightingService, times(1)).getPublicSightings(any());
    }

    @Test
    @WithAnonymousUser
    void testGetSightingById_NoAuth_Returns200() throws Exception {
        // Arrange
        when(sightingService.getById(1L)).thenReturn(testSighting);

        // Act & Assert
        mockMvc.perform(get("/api/sightings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.airportIataOrIcao").value("LAX"));
    }

    @Test
    @WithMockUser(username = "user_123")
    void testCreateSighting_InvalidData_Returns400() throws Exception {
        // Arrange - Missing required fields
        SightingRequest request = new SightingRequest();
        // No timestamp or airport

        // Act & Assert
        mockMvc.perform(post("/api/sightings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
