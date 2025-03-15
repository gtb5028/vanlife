package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.LocationType;
import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.beerbower.vanlife.repositories.LocationTypeRepository;
import org.beerbower.vanlife.repositories.UserRepository;
import org.beerbower.vanlife.services.overpass.OverpassService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationController {



    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OverpassService overpassService;
    private final LocationUtils locationUtils;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Location> getLocations(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> typeIds,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double minLon,
            @RequestParam(required = false) Double maxLon) {

        if (name != null) {
            return locationRepository.findByNameContainingIgnoreCase(name);
        }

        if (typeIds != null && minLat != null && maxLat != null && minLon != null && maxLon != null) {
            List<LocationType> types = locationTypeRepository.findAllById(typeIds);

            Map<Long, Location> externalReferenceLocations =
                    locationRepository.findByTypeInAndLatitudeBetweenAndLongitudeBetweenAndExternal(types, minLat, maxLat, minLon, maxLon).
                            stream().collect(Collectors.toMap(Location::getExternalId, l -> l));

            List<Location> locations = new java.util.ArrayList<>(
                    overpassService.fetchNodes(types, minLat, minLon, maxLat, maxLon).
                            stream().map( n -> locationUtils.mapNodeToLocation(n, externalReferenceLocations.get(n.id()))).toList());

            locations.addAll(locationRepository.findByTypeInAndLatitudeBetweenAndLongitudeBetween(types, minLat, maxLat, minLon, maxLon));
            return locations;
        }

        if (typeIds != null) {
            List<LocationType> types = locationTypeRepository.findAllById(typeIds);
            return locationRepository.findByTypeIn(types);
        }
        return locationRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Location getSingleLocation(@PathVariable String id) {
        return locationUtils.getLocation(id);
    }



    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocation(@RequestBody LocationDto location, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Location newLocation = new Location();
        newLocation.setSource(Location.Source.LOC);
        newLocation.setName(location.name);
        newLocation.setLatitude(location.latitude);
        newLocation.setLongitude(location.longitude);
        newLocation.setDescription(location.description);
        LocationType locationType = locationTypeRepository.findById(location.typeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location type ID"));
        newLocation.setType(locationType);
        newLocation.setCreatedBy(user);
        return locationRepository.save(newLocation);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("{id}/metadata")
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocationMetadata(@RequestBody Map<String, String> metadata, @PathVariable String id, Principal principal) {
        LocationId locationId = LocationUtils.parseLocationId(id);
        Location location = null;
        if (locationId.source == Location.Source.OSM) {
            location = locationUtils.getReferenceLocation(principal, locationId);
        } else {
            location = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        location.getMetadata().putAll(metadata);
        return locationRepository.save(location);
    }


    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable String id, @RequestBody LocationDto location, Principal principal) {

        LocationId locationId = LocationUtils.parseLocationId(id);
        Location existingLocation;
        if (locationId.source == Location.Source.OSM) {
            existingLocation = locationUtils.getReferenceLocation(principal, locationId);
        } else {
            existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        existingLocation.setName(location.name);
        existingLocation.setLatitude(location.latitude);
        existingLocation.setLongitude(location.longitude);
        existingLocation.setDescription(location.description);
        LocationType locationType = locationTypeRepository.findById(location.typeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location type ID"));
        existingLocation.setType(locationType);

        locationRepository.save(existingLocation);
        return locationUtils.getLocation(id);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public Location patchLocation(@PathVariable String id, @RequestBody LocationDto location, Principal principal) {

        LocationId locationId = LocationUtils.parseLocationId(id);
        Location existingLocation;
        if (locationId.source == Location.Source.OSM) {
            existingLocation = locationUtils.getReferenceLocation(principal, locationId);
        } else {
            existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }
        if (location.name != null) {
            existingLocation.setName(location.name);
        }

        if (location.latitude != null) {
            existingLocation.setLatitude(location.latitude);
        }

        if (location.longitude != null) {
            existingLocation.setLongitude(location.longitude);
        }

        if (location.description != null) {
            existingLocation.setDescription(location.description);
        }

        if (location.typeId != null) {
            LocationType locationType = locationTypeRepository.findById(location.typeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location type ID"));
            existingLocation.setType(locationType);
        }
        locationRepository.save(existingLocation);
        return locationUtils.getLocation(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable String id) {

        LocationId locationId = LocationUtils.parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            Location location = locationRepository.findByExternalId(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            locationRepository.delete(location);
        } else {
            if (!locationRepository.existsById(locationId.id)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            locationRepository.deleteById(locationId.id);
        }
    }



    public record LocationDto(String name, Double latitude, Double longitude, String description, Long typeId) {
    }

    public record LocationId(Location.Source source, long id) {
    }
}
