package org.beerbower.vanlife.repositories;

import org.beerbower.vanlife.entities.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationTypeRepository extends JpaRepository<LocationType, Long> {
    List<LocationType> findByNameIgnoreCase(String name);
}
