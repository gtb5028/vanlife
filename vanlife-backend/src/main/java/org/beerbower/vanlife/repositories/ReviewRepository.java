package org.beerbower.vanlife.repositories;

import org.beerbower.vanlife.entities.Review;
import org.beerbower.vanlife.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByLocation(Location location);
}
