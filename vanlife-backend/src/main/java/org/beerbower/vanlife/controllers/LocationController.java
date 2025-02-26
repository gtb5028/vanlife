package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.beerbower.vanlife.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationController {
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Location getSingleLocation(@PathVariable long id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocation(@RequestBody Location location, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        location.setId(null);
        location.setCreatedBy(user);
        location.setCreatedAt(null);
        location.setUpdatedAt(null);
        return locationRepository.save(location);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        locationRepository.deleteById(id);
    }
}
