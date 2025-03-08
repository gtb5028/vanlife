package org.beerbower.vanlife.repositories;

import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Find locations by name (case-insensitive)
    List<Location> findByNameContainingIgnoreCase(String name);

    // Find locations by type
    List<Location> findByType(LocationType type);

    List<Location> findByTypeIn(List<LocationType> types);

    // Find locations within a certain area (example: using latitude and longitude)
    List<Location> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLon, Double maxLon);

    Optional<Location> findByExternalId(Long externalId);

    @Query("SELECT l FROM Location l WHERE l.type IN :types " +
            "AND l.latitude BETWEEN :minLat AND :maxLat " +
            "AND l.longitude BETWEEN :minLon AND :maxLon " +
            "AND l.externalId IS NULL")
    List<Location> findByTypeInAndLatitudeBetweenAndLongitudeBetween(
            @Param("types") List<LocationType> types,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon);

    @Query("SELECT l FROM Location l WHERE l.type IN :types " +
            "AND l.latitude BETWEEN :minLat AND :maxLat " +
            "AND l.longitude BETWEEN :minLon AND :maxLon " +
            "AND l.externalId IS NOT NULL")
    List<Location> findByTypeInAndLatitudeBetweenAndLongitudeBetweenAndExternal(
            @Param("types") List<LocationType> types,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon);
}
