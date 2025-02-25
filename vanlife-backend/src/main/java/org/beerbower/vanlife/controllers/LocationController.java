package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationController {
    private final LocationRepository locationRepository;

    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @GetMapping("/{id}")
    public Location getSingleLocation(@PathVariable long id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocation(@RequestBody Location location) {
        return locationRepository.save(location);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        locationRepository.deleteById(id);
    }
}
