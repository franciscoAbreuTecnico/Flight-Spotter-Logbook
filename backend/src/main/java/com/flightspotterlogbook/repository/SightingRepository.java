package com.flightspotterlogbook.repository;

import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing sightings. Provides convenience methods for filtering
 * by owner and visibility.
 */
@Repository
public interface SightingRepository extends JpaRepository<Sighting, Long> {
    Page<Sighting> findByOwnerUserId(String ownerUserId, Pageable pageable);

    Page<Sighting> findByVisibility(Visibility visibility, Pageable pageable);
}