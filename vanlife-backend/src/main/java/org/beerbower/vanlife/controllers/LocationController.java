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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin
@PreAuthorize("permitAll()")
@RequiredArgsConstructor
public class LocationController {

    private static final Pattern ID_PATTERN = Pattern.compile("^([A-Z]{3})-(\\d+)$");

    private static Map<Map.Entry<String, String>, List<LocationType>> locationTypeCache;

    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OverpassService overpassService;

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
                            stream().map( n -> mapNodeToLocation(n, externalReferenceLocations.get(n.id()))).toList());

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

        try {
            LocationId locationId = parseLocationId(id);
            if (locationId.source == Location.Source.OSM) {
                OverpassService.Node node = overpassService.fetchNode(locationId.id).
                        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                Location referenceLocation = locationRepository.findByExternalId(locationId.id).orElse(null);
                return mapNodeToLocation(node, referenceLocation);
            }
            return locationRepository.findById(locationId.id).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
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
        LocationId locationId = parseLocationId(id);
        Location location = null;
        if (locationId.source == Location.Source.OSM) {
            location = locationRepository.findByExternalId(locationId.id).orElse(null);
            if (location == null) {
                OverpassService.Node node = overpassService.fetchNode(locationId.id).
                        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                Location externalLocation = mapNodeToLocation(node, null);
                externalLocation.setInternalId(null);
                externalLocation.setSource(Location.Source.OSM);
                externalLocation.setExternalId(locationId.id);
                User user = userRepository.findByEmail(principal.getName()).
                        orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                externalLocation.setCreatedBy(user);
                location = locationRepository.save(externalLocation);
            }
        } else {
            location = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        Map<String, String> existingMetadata = location.getMetadata();
        if (existingMetadata == null) {
            existingMetadata = metadata;
        } else {
            existingMetadata.putAll(metadata);
        }
        location.setMetadata(existingMetadata);
        return locationRepository.save(location);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public Location updateLocation(@PathVariable String id, @RequestBody LocationDto location) {

        LocationId locationId = parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update external location");
        }

        Location existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existingLocation.setName(location.name);
        existingLocation.setLatitude(location.latitude);
        existingLocation.setLongitude(location.longitude);
        existingLocation.setDescription(location.description);
        LocationType locationType = locationTypeRepository.findById(location.typeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid location type ID"));
        existingLocation.setType(locationType);

        return locationRepository.save(existingLocation);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}")
    public Location patchLocation(@PathVariable String id, @RequestBody LocationDto location) {

        LocationId locationId = parseLocationId(id);
        if (locationId.source == Location.Source.OSM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot patch external location");
        }

        Location existingLocation = locationRepository.findById(locationId.id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

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

    private Location mapNodeToLocation(OverpassService.Node node, Location referenceLocation) {
        Location location = new Location();
        location.setExternalId(node.id());
        location.setSource(Location.Source.OSM);
        location.setLatitude(node.lat());
        location.setLongitude(node.lon());
        Map<String, String> tags = node.tags();
        if (tags != null) {
            location.setName(tags.getOrDefault("name", "Unknown"));
            location.setDescription(tags.getOrDefault("description", ""));

            LocationType locationType = getLocationType(tags);
            if (locationType == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown location type");
            }
            location.setType(locationType);

            location.setMetadata(tags);
        }
        if (referenceLocation != null) {
            Map<String, String> metadata = new HashMap<>(location.getMetadata());
            referenceLocation.getMetadata().forEach(metadata::putIfAbsent);
            location.setMetadata(metadata);
        }
        return location;
    }

    private synchronized LocationType getLocationType(Map<String, String> tags) {
        if (locationTypeCache == null) {
            locationTypeCache = new java.util.HashMap<>();
            List<LocationType> locationTypes = locationTypeRepository.findAll();
            for (LocationType type : locationTypes) {
                for (Map.Entry<String, String> entry : type.getOverpassTags().entrySet()) {
                    locationTypeCache.computeIfAbsent(entry, k -> new java.util.ArrayList<>()).add(type);
                }
            }
        }
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            List<LocationType> types = locationTypeCache.get(entry);
            if (types != null) {
                for (LocationType type : types) {
                    if (tags.entrySet().containsAll(type.getOverpassTags().entrySet())) {
                        return type;
                    }
                }
            }
        }
        return null;
    }

    public record LocationDto(String name, Double latitude, Double longitude, String description, Long typeId) {
    }

    private record LocationId(Location.Source source, long id) {
    }
}
