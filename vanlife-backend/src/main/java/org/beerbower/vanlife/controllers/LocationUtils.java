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
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class LocationUtils {
    private static final Pattern ID_PATTERN = Pattern.compile("^([A-Z]{3})-(\\d+)$");

    private static Map<Map.Entry<String, String>, List<LocationType>> locationTypeCache;


    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OverpassService overpassService;


    public static LocationController.LocationId parseLocationId(String id) throws IllegalArgumentException {
        try {
            long parsedId = Long.parseLong(id);
            return new LocationController.LocationId(Location.Source.LOC, parsedId);
        } catch (NumberFormatException e) {
            Matcher matcher = ID_PATTERN.matcher(id);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid location ID: " + id);
            }
            return new LocationController.LocationId(Location.Source.valueOf(matcher.group(1)), Long.parseLong(matcher.group(2)));
        }
    }

    public Location mapNodeToLocation(OverpassService.Node node, Location referenceLocation) {
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
            location.setName(referenceLocation.getName());
            location.setDescription(referenceLocation.getDescription());
            location.setType(referenceLocation.getType());
            location.setLatitude(referenceLocation.getLatitude());
            location.setLongitude(referenceLocation.getLongitude());

            Map<String, String> metadata = new HashMap<>(location.getMetadata());
            referenceLocation.getMetadata().forEach(metadata::putIfAbsent);
            location.setMetadata(metadata);

            location.setCreatedBy(referenceLocation.getCreatedBy());
            location.setCreatedAt(referenceLocation.getCreatedAt());
            location.setUpdatedAt(referenceLocation.getUpdatedAt());
        }
        return location;
    }

    public synchronized LocationType getLocationType(Map<String, String> tags) {
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

    public Location getLocation(String id) {
        try {
            LocationController.LocationId locationId = parseLocationId(id);
            if (locationId.source() == Location.Source.OSM) {
                OverpassService.Node node = overpassService.fetchNode(locationId.id()).
                        orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
                Location referenceLocation = locationRepository.findByExternalId(locationId.id()).orElse(null);
                return mapNodeToLocation(node, referenceLocation);
            }
            return locationRepository.findById(locationId.id()).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    public Location getReferenceLocation(Principal principal, LocationController.LocationId locationId) {
        Location location = locationRepository.findByExternalId(locationId.id()).orElse(null);
        if (location == null) {
            OverpassService.Node node = overpassService.fetchNode(locationId.id()).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            Location externalLocation = mapNodeToLocation(node, null);
            externalLocation.setInternalId(null);
            externalLocation.setSource(Location.Source.OSM);
            externalLocation.setExternalId(locationId.id());
            User user = userRepository.findByEmail(principal.getName()).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            externalLocation.setCreatedBy(user);
            location = locationRepository.save(externalLocation);
        }
        return location;
    }





}
