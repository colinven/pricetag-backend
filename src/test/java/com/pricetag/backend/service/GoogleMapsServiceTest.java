package com.pricetag.backend.service;

import com.pricetag.backend.dto.LatLng;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class GoogleMapsServiceTest {

    @Autowired
    private GoogleMapsService googleMapsService;

    @Test
    void testGeocode() {
        LatLng coordinates = googleMapsService.geocode("20005 Metaphor Mews, Davidson, NC 28036");
        assertThat(coordinates.lat()).isEqualTo(35.479219);
        assertThat(coordinates.lng()).isEqualTo(-80.8191537);
    }
}
