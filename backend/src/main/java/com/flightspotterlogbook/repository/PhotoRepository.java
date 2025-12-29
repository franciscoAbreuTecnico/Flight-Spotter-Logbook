package com.flightspotterlogbook.repository;

import com.flightspotterlogbook.model.Photo;
import com.flightspotterlogbook.model.Sighting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for photos.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findBySighting(Sighting sighting);
}