package com.pricetag.backend.service;

import com.pricetag.backend.dto.LatLng;
import org.junit.jupiter.api.Tag;
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
        LatLng coords = googleMapsService.geocode("20005 Metaphor Mews, Davidson, NC 28036");
        System.out.println("lat:" + coords.lat());
        System.out.println("lng:" + coords.lng());
        assertThat(coords.lat()).isNotNull();
        assertThat(coords.lng()).isNotNull();
    }
}
