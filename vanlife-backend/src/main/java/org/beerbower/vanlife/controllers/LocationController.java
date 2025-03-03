package org.beerbower.vanlife.controllers;

import lombok.RequiredArgsConstructor;
import org.beerbower.vanlife.entities.Location;
import org.beerbower.vanlife.entities.User;
import org.beerbower.vanlife.repositories.LocationRepository;
import org.beerbower.vanlife.repositories.UserRepository;
import org.beerbower.vanlife.services.overpass.OverpassService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationController {

    private static final Map<String, List<Map<String, String>>> LOCATION_TYPE_TO_TAGS = Map.of(
            "restaurant", List.of(Map.of("amenity", "restaurant")),
            "rest_stop", List.of(Map.of("highway", "rest_area")),
            "campground", List.of(Map.of("tourism", "camp_site")),
            "gym", List.of(Map.of("amenity", "gym")),
            "park", List.of(Map.of("leisure", "park"), Map.of("boundary", "national_park"))
    );

    private static final Pattern ID_PATTERN = Pattern.compile("^([A-Z]{3})-(\\d+)$");

    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OverpassService overpassService;


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

        if (type != null && minLat != null && maxLat != null && minLon != null && maxLon != null) {
            List<Map<String, String>> tags = LOCATION_TYPE_TO_TAGS.get(type);
            List<Location> locations = new java.util.ArrayList<>(
                    overpassService.fetchNodes(tags, minLat, minLon, maxLat, maxLon).
                    stream().map(this::mapNodeToLocation).toList());

            locations.addAll(locationRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon));
            return locations;
        }

        if (type != null) {
            return locationRepository.findByTypeContainingIgnoreCase(type);
        }
        return locationRepository.findAll();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Location getSingleLocation(@PathVariable String id) {

        try {
            LocationId locationId = parseLocationId(id);
            switch (locationId.source) {
                case OSM:
                    OverpassService.Node node = overpassService.fetchNode(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                    return mapNodeToLocation(node);
                case LOC:
                default:
                    return locationRepository.findById(locationId.id).
                            orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Location createLocation(@RequestBody Location location, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        location.setInternalId(null);
        location.setCreatedBy(user);
        location.setCreatedAt(null);
        location.setUpdatedAt(null);
        return locationRepository.save(location);
    }
    
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable String id, @RequestBody Location location) {

        LocationId locationId = parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update external location");
        }

        Location existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existingLocation.setName(location.getName());
        existingLocation.setLatitude(location.getLatitude());
        existingLocation.setLongitude(location.getLongitude());
        existingLocation.setType(location.getType());
        existingLocation.setDescription(location.getDescription());
        return locationRepository.save(existingLocation);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public Location patchLocation(@PathVariable String id, @RequestBody Location location) {

        LocationId locationId = parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot patch external location");
        }

        Location existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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
    public void deleteLocation(@PathVariable String id) {

        LocationId locationId = parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete external location");
        }

        if (!locationRepository.existsById(locationId.id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        locationRepository.deleteById(locationId.id);
    }


    private static LocationId parseLocationId(String id) throws IllegalArgumentException {
        try {
            long parsedId = Long.parseLong(id);
            return new LocationId(Location.Source.LOC, parsedId);
        } catch (NumberFormatException e) {
            Matcher matcher = ID_PATTERN.matcher(id);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid location ID: " + id);
            }
            return new LocationId(Location.Source.valueOf(matcher.group(1)), Long.parseLong(matcher.group(2)));
        }
    }

    private Location mapNodeToLocation(OverpassService.Node node) {
        Location location = new Location();
        location.setExternalId(node.id());
        location.setSource(Location.Source.OSM);
        location.setLatitude(node.lat());
        location.setLongitude(node.lon());
        Map<String, String> tags = node.tags();
        if (tags != null) {
            location.setName(tags.getOrDefault("name", "Unknown"));
            location.setType(tags.getOrDefault("amenity", "Unknown"));
            location.setDescription(tags.getOrDefault("description", ""));
        }
        return location;
    }

    private record LocationId(Location.Source source, long id) {}
}
