package org.beerbower.vanlife.services.overpass;

import org.beerbower.vanlife.entities.LocationType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class OverpassService {
    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private final RestTemplate restTemplate;

    public OverpassService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Node> fetchNodes(List<LocationType> types, double minLat, double minLon, double maxLat, double maxLon) {

        StringBuilder query = new StringBuilder("[out:json];(");
        for (LocationType type : types) {

            Map<String, String> overpassTags = type.getOverpassTags();
            if (overpassTags != null && !overpassTags.isEmpty()) {
                query.append("node");
                for (Map.Entry<String, String> entry : overpassTags.entrySet()) {
                    query.append("[\"").
                            append(entry.getKey()).
                            append("\"=\"").
                            append(entry.getValue()).
                            append("\"]");
                }
                query.append("(").
                        append(minLat).
                        append(",").
                        append(minLon).
                        append(",").
                        append(maxLat).
                        append(",").
                        append(maxLon).
                        append(");");
            }
        }
        query.append(");out body;");

        String encodedQuery = UriComponentsBuilder.fromUriString(OVERPASS_URL)
                .queryParam("data", query)
                .build()
                .toUriString();

        ResponseEntity<OverpassResponse> response = restTemplate.getForEntity(encodedQuery, OverpassResponse.class);

        OverpassResponse body = response.getBody();
        return body == null ? new ArrayList<>() : response.getBody().elements();
    }

    public Optional<Node> fetchNode(long id) {
        String query = String.format(
                "[out:json];node(%d);out body;",
                id
        );

        String encodedQuery = UriComponentsBuilder.fromUriString(OVERPASS_URL)
                .queryParam("data", query)
                .build()
                .toUriString();

        ResponseEntity<OverpassResponse> response = restTemplate.getForEntity(encodedQuery, OverpassResponse.class);

        OverpassResponse body = response.getBody();
        return body == null ? Optional.empty() : Optional.of(body.elements.get(0));
    }

    public record Node(long id, double lat, double lon, Map<String, String> tags) {
    }

    public record OverpassResponse(List<Node> elements) {
    }
}