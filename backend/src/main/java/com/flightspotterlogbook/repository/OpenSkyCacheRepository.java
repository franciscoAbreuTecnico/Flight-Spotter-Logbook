package com.flightspotterlogbook.repository;

import com.flightspotterlogbook.model.OpenSkyCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for persisting OpenSky cache entries.
 */
@Repository
public interface OpenSkyCacheRepository extends JpaRepository<OpenSkyCache, Long> {
    Optional<OpenSkyCache> findByQueryHash(String queryHash);
}