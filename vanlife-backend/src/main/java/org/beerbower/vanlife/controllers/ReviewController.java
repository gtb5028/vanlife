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
import java.util.List;

@RestController
@RequestMapping("/locations/{locationId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Review> getReviewsByLocation(@PathVariable Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not found"));

        return reviewRepository.findByLocation(location);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review addReview(@RequestBody Review review, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        review.setId(null);
        review.setCreatedBy(user);
        review.setCreatedAt(null);
        review.setUpdatedAt(null);

        return reviewRepository.save(review);
    }
}
