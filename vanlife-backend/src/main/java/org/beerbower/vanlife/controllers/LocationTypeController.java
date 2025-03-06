package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.LocationType;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.beerbower.vanlife.repositories.LocationTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/locationTypes")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationTypeController {
    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<LocationType> getLocationTypes() {
        return locationTypeRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public LocationType getSingleLocationType(@PathVariable Long id) {
        return locationTypeRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocationType createLocationType(@RequestBody LocationType locationType) {
        return locationTypeRepository.save(locationType);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public LocationType updateLocationType(@PathVariable long id, @RequestBody LocationType locationType) {
        LocationType existingLocationType = locationTypeRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existingLocationType.setName(locationType.getName());
        existingLocationType.setDescription(locationType.getDescription());
        existingLocationType.setOverpassTags(locationType.getOverpassTags());

        return locationTypeRepository.save(existingLocationType);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    public LocationType patchLocationType(@PathVariable long id, @RequestBody LocationType locationType) {
        LocationType existingLocationType = locationTypeRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (locationType.getName() != null) {
            existingLocationType.setName(locationType.getName());
        }

        if (locationType.getDescription() != null) {
            existingLocationType.setDescription(locationType.getDescription());
        }

        if (locationType.getOverpassTags() != null) {
            existingLocationType.setOverpassTags(locationType.getOverpassTags());
        }

        return locationTypeRepository.save(existingLocationType);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocationType(@PathVariable Long id) {
        LocationType locationType = locationTypeRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Location> locations = locationRepository.findByType(locationType);

        if (!locations.isEmpty())
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete LocationType as it is associated with existing locations.");
        }

        locationTypeRepository.deleteById(id);
    }
}
