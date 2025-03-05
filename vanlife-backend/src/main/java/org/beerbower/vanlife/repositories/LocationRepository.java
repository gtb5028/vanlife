package org.beerbower.vanlife.repositories;

import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Find locations by name (case-insensitive)
    List<Location> findByNameContainingIgnoreCase(String name);

    // Find locations by type
    List<Location> findByType(LocationType type);

    // Find locations within a certain area (example: using latitude and longitude)
    List<Location> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLon, Double maxLon);
}
