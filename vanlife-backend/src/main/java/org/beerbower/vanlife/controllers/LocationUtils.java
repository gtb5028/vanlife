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
    /**
     * Pattern for parsing location IDs.
     * <p>
     * The pattern matches strings of the form "LOC-12345" where "LOC" is a three-letter
     * source code and "12345" is a numeric ID.
     */
    private static final Pattern ID_PATTERN = Pattern.compile("^([A-Z]{3})-(\\d+)$");

    /**
     * Cache for location types.
     * <p>
     * The cache maps a pair of tag key and value to a list of LocationType objects.
     * This is used to avoid repeated database queries for the same tag.
     */
    private static Map<Map.Entry<String, String>, List<LocationType>> locationTypeCache;

    private final LocationTypeRepository locationTypeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final OverpassService overpassService;

    /**
     * Parse a location ID string into a LocationId object.
     * @param id The location ID string to parse.
     * @return The parsed LocationId object.
     * @throws IllegalArgumentException if the ID is invalid or cannot be parsed.
     */
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

    /**
     * Maps an OverpassService.Node to a Location object.
     * @param node The OverpassService.Node to map.
     * @param referenceLocation The reference location to use for additional information.
     * @return The mapped Location object.
     */
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

    /**
     * Get the location type based on the provided tags.
     * @param tags The tags to use for determining the location type.
     * @return The matching LocationType, or null if no match is found.
     */
    public synchronized LocationType getLocationType(Map<String, String> tags) {
        if (locationTypeCache == null) {
            locationTypeCache = new java.util.HashMap<>();
            List<LocationType> locationTypes = locationTypeRepository.findAll();
            for (LocationType type : locationTypes) {
                for (Map.Entry<String, String> entry : type.getOverpassTags().entrySet()) {
                    locationTypeCache.computeIfAbsent(entry, _ -> new java.util.ArrayList<>()).add(type);
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

    /**
     * Get a location by its ID.
     * @param id The ID of the location to retrieve.
     * @return The Location object.
     * @throws ResponseStatusException if the location is not found.
     */
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

    /**
     * Get or create a reference location based on the provided location ID.
     * @param principal The principal of the user requesting the location.
     * @param locationId The ID of the location to retrieve or create.
     * @return The Location object.
     * @throws ResponseStatusException if the location is not found or cannot be created.
     */
    public Location getOrCreateReferenceLocation(Principal principal, LocationController.LocationId locationId) {
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
