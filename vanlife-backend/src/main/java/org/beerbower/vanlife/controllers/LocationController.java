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
    public List<Location> getLocations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double minLon,
            @RequestParam(required = false) Double maxLon) {
        
        if (name != null) {
            return locationRepository.findByNameContainingIgnoreCase(name);
        }

        if (type != null) {
            return locationRepository.findByTypeContainingIgnoreCase(type);
        }

        if (minLat != null && maxLat != null && minLon != null && maxLon != null) {
            return locationRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon);
        }

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
    
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable long id, @RequestBody Location location) {
        Location existingLocation = locationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existingLocation.setName(location.getName());
        existingLocation.setLatitude(location.getLatitude());
        existingLocation.setLongitude(location.getLongitude());
        existingLocation.setType(location.getType());
        existingLocation.setDescription(location.getDescription());
        return locationRepository.save(existingLocation);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public Location patchLocation(@PathVariable long id, @RequestBody Location location) {
        Location existingLocation = locationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (location.getName() != null) {
            existingLocation.setName(location.getName());
        }

        if (location.getLatitude() != null) {
            existingLocation.setLatitude(location.getLatitude());
        }

        if (location.getLongitude() != null) {
            existingLocation.setLongitude(location.getLongitude());
        }

        if (location.getType() != null) {
            existingLocation.setType(location.getType());
        }

        if (location.getDescription() != null) {
            existingLocation.setDescription(location.getDescription());
        }

        return locationRepository.save(existingLocation);
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
