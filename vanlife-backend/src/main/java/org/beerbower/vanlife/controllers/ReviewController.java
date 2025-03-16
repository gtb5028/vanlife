package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.Review;
import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.beerbower.vanlife.repositories.ReviewRepository;
import org.beerbower.vanlife.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/locations/{id}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationUtils locationUtils;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Review> getReviewsByLocation(@PathVariable String id) {

        LocationController.LocationId locationId = LocationUtils.parseLocationId(id);
        Location location = null;
        if (locationId.source() == Location.Source.OSM) {
            location = locationRepository.findByExternalId(locationId.id()).orElse(null);
        } else {
            location = locationRepository.findById(locationId.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        return location == null ? new ArrayList<>() : reviewRepository.findByLocation(location);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review addReview(@PathVariable String id, @RequestBody Review review, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        LocationController.LocationId locationId = LocationUtils.parseLocationId(id);
        Location location = null;
        if (locationId.source() == Location.Source.OSM) {
            location = locationUtils.getReferenceLocation(principal, locationId);
        } else {
            location = locationRepository.findById(locationId.id()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        review.setId(null);
        review.setLocation(location);
        review.setCreatedBy(user);
        review.setCreatedAt(null);
        review.setUpdatedAt(null);

        return reviewRepository.save(review);
    }
}
