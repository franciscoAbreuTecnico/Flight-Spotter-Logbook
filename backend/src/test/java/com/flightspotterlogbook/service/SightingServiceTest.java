package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.EnrichmentStatus;
import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.Visibility;
import com.flightspotterlogbook.repository.SightingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SightingService.
 * Tests CRUD operations, authorization logic, and pagination.
 */
@ExtendWith(MockitoExtension.class)
class SightingServiceTest {

    @Mock
    private SightingRepository sightingRepository;

    @Mock
    private OpenSkyService openSkyService;

    @InjectMocks
    private SightingService sightingService;

    private Sighting testSighting;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "user_123";
        testSighting = Sighting.builder()
                .id(1L)
                .ownerUserId(testUserId)
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("LAX")
                .locationText("Los Angeles, USA")
                .airline("American Airlines")
                .callsign("AAL123")
                .registration("N12345")
                .aircraftModel("Boeing 737")
                .notes("Beautiful landing")
                .visibility(Visibility.PUBLIC)
                .enrichmentStatus(EnrichmentStatus.ENRICHING)
                .build();
    }

    @Test
    void testCreateSighting_Success() {
        // Arrange
        Sighting newSighting = Sighting.builder()
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("JFK")
                .locationText("New York, USA")
                .visibility(Visibility.PUBLIC)
                .build();

        when(sightingRepository.save(any(Sighting.class))).thenReturn(testSighting);

        // Act
        Sighting result = sightingService.createSighting(newSighting, testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(testUserId, result.getOwnerUserId());
        assertEquals(EnrichmentStatus.ENRICHING, result.getEnrichmentStatus());
        
        // Verify enrichment was triggered
        verify(openSkyService, times(1)).enrichAsync(any(Sighting.class));
        verify(sightingRepository, times(1)).save(any(Sighting.class));
    }

    @Test
    void testGetSightingsForUser_Paginated() {
        // Arrange
        List<Sighting> sightings = Arrays.asList(testSighting);
        Page<Sighting> page = new PageImpl<>(sightings);
        Pageable pageable = PageRequest.of(0, 10);

        when(sightingRepository.findByOwnerUserId(testUserId, pageable)).thenReturn(page);

        // Act
        Page<Sighting> result = sightingService.getSightingsForUser(testUserId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testSighting.getId(), result.getContent().get(0).getId());
        verify(sightingRepository, times(1)).findByOwnerUserId(testUserId, pageable);
    }

    @Test
    void testGetPublicSightings_OnlyReturnsPublic() {
        // Arrange
        List<Sighting> publicSightings = Arrays.asList(testSighting);
        Page<Sighting> page = new PageImpl<>(publicSightings);
        Pageable pageable = PageRequest.of(0, 10);

        when(sightingRepository.findByVisibility(Visibility.PUBLIC, pageable)).thenReturn(page);

        // Act
        Page<Sighting> result = sightingService.getPublicSightings(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(sightingRepository, times(1)).findByVisibility(Visibility.PUBLIC, pageable);
    }

    @Test
    void testGetById_Success() {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act
        Sighting result = sightingService.getById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testSighting.getId(), result.getId());
        assertEquals(testSighting.getOwnerUserId(), result.getOwnerUserId());
    }

    @Test
    void testGetById_NotFound_ThrowsException() {
        // Arrange
        when(sightingRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            sightingService.getById(999L);
        });
    }

    @Test
    void testUpdateSighting_AsOwner_Success() {
        // Arrange
        Sighting updatedSighting = Sighting.builder()
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("SFO")
                .locationText("San Francisco, USA")
                .visibility(Visibility.PRIVATE)
                .build();

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(sightingRepository.save(any(Sighting.class))).thenReturn(testSighting);

        // Act
        Sighting result = sightingService.updateSighting(1L, updatedSighting, testUserId, false);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<Sighting> captor = ArgumentCaptor.forClass(Sighting.class);
        verify(sightingRepository).save(captor.capture());
        
        Sighting saved = captor.getValue();
        assertEquals("SFO", saved.getAirportIataOrIcao());
        assertEquals(Visibility.PRIVATE, saved.getVisibility());
    }

    @Test
    void testUpdateSighting_AsNonOwner_ThrowsException() {
        // Arrange
        Sighting updatedSighting = Sighting.builder()
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("SFO")
                .visibility(Visibility.PUBLIC)
                .build();

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            sightingService.updateSighting(1L, updatedSighting, "different_user", false);
        });
    }

    @Test
    void testUpdateSighting_AsAdmin_Success() {
        // Arrange
        Sighting updatedSighting = Sighting.builder()
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("ORD")
                .locationText("Chicago, USA")
                .visibility(Visibility.PUBLIC)
                .build();

        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));
        when(sightingRepository.save(any(Sighting.class))).thenReturn(testSighting);

        // Act
        Sighting result = sightingService.updateSighting(1L, updatedSighting, "admin_user", true);

        // Assert
        assertNotNull(result);
        verify(sightingRepository).save(any(Sighting.class));
    }

    @Test
    void testDeleteSighting_AsOwner_Success() {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act
        sightingService.deleteSighting(1L, testUserId, false);

        // Assert
        verify(sightingRepository, times(1)).delete(testSighting);
    }

    @Test
    void testDeleteSighting_AsNonOwner_ThrowsException() {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            sightingService.deleteSighting(1L, "different_user", false);
        });
    }

    @Test
    void testDeleteSighting_AsAdmin_Success() {
        // Arrange
        when(sightingRepository.findById(1L)).thenReturn(Optional.of(testSighting));

        // Act
        sightingService.deleteSighting(1L, "admin_user", true);

        // Assert
        verify(sightingRepository, times(1)).delete(testSighting);
    }

    @Test
    void testGetAllSightings_ReturnsAllVisibility() {
        // Arrange
        Sighting privateSighting = Sighting.builder()
                .id(2L)
                .ownerUserId("user_456")
                .timestamp(LocalDateTime.now())
                .airportIataOrIcao("MIA")
                .visibility(Visibility.PRIVATE)
                .enrichmentStatus(EnrichmentStatus.ENRICHED)
                .build();

        List<Sighting> allSightings = Arrays.asList(testSighting, privateSighting);
        Page<Sighting> page = new PageImpl<>(allSightings);
        Pageable pageable = PageRequest.of(0, 50);

        when(sightingRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Sighting> result = sightingService.getAllSightings(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(sightingRepository, times(1)).findAll(pageable);
    }
}
