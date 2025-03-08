package org.beerbower.vanlife.services.overpass;

import org.beerbower.vanlife.entities.LocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OverpassServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OverpassService overpassService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFetchNodes() {


        // Arrange
        List<LocationType> locationTypes = List.of(
                new LocationType( 1L, "Rest Area", "Rest area type.", Map.of("amenity", "rest_area")),
                new LocationType( 2L, "Camp Site", "Camp site type.", Map.of("tourism", "camp_site"))
                );
        double minLat = 10.0;
        double minLon = 20.0;
        double maxLat = 30.0;
        double maxLon = 40.0;

        OverpassService.Node node1 = new OverpassService.Node(1, 15.0, 25.0, Map.of("name", "Rest Area 1"));
        OverpassService.Node node2 = new OverpassService.Node(2, 35.0, 45.0, Map.of("name", "Camp Site 1"));
        OverpassService.OverpassResponse mockResponse = new OverpassService.OverpassResponse(List.of(node1, node2));

        ResponseEntity<OverpassService.OverpassResponse> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Mock the RestTemplate call
        when(restTemplate.getForEntity(anyString(), eq(OverpassService.OverpassResponse.class)))
                .thenReturn(mockResponseEntity);

        // Act
        List<OverpassService.Node> result = overpassService.fetchNodes(locationTypes, minLat, minLon, maxLat, maxLon);

        // Assert
        assertEquals(2, result.size());
        assertEquals(node1, result.get(0));
        assertEquals(node2, result.get(1));
    }

    @Test
    public void testFetchNode() {
        // Arrange
        long id = 123L;
        OverpassService.Node node = new OverpassService.Node(id, 15.0, 25.0, Map.of("name", "POI 1"));
        OverpassService.OverpassResponse mockResponse = new OverpassService.OverpassResponse(List.of(node));

        ResponseEntity<OverpassService.OverpassResponse> mockResponseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        // Mock the RestTemplate call
        when(restTemplate.getForEntity(anyString(), eq(OverpassService.OverpassResponse.class)))
                .thenReturn(mockResponseEntity);

        // Act
        Optional<OverpassService.Node> result = overpassService.fetchNode(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(node, result.get());
    }

    @Test
    public void testFetchNode_NotFound() {
        // Arrange
        long id = 123L;
        ResponseEntity<OverpassService.OverpassResponse> mockResponseEntity = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        // Mock the RestTemplate call
        when(restTemplate.getForEntity(anyString(), eq(OverpassService.OverpassResponse.class)))
                .thenReturn(mockResponseEntity);

        // Act
        Optional<OverpassService.Node> result = overpassService.fetchNode(id);

        // Assert
        assertFalse(result.isPresent());
    }
}