package com.pricetag.backend.util;

import com.pricetag.backend.dto.LatLng;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.exception.GeocodingException;
import com.pricetag.backend.service.GoogleMapsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeoUtilsTest {

    @Mock
    private GoogleMapsService googleMapsService;

    @InjectMocks
    private GeoUtils geoUtils;

    private Company company;

    @BeforeEach
    void setup() {
        company = new Company();
        company.setServiceRadiusMiles(30);
        company.setServiceAreaLatitude(35.2271);  // Charlotte, NC
        company.setServiceAreaLongitude(-80.8431);
    }

    @Test
    void propertyIsInServiceArea_returnsTrue_whenWithinRadius() {
        // Concord, NC - ~19 miles NE of Charlotte
        when(googleMapsService.geocode("123 Concord Pkwy, Concord, NC"))
                .thenReturn(new LatLng(35.4088, -80.5796));

        assertThat(geoUtils.propertyIsInServiceArea(company, "123 Concord Pkwy, Concord, NC")).isTrue();
    }

    @Test
    void propertyIsInServiceArea_returnsFalse_whenOutsideRadius() {
        // Greensboro, NC - ~83 miles NE of Charlotte
        when(googleMapsService.geocode("400 W Market St, Greensboro, NC"))
                .thenReturn(new LatLng(36.0726, -79.7920));

        assertThat(geoUtils.propertyIsInServiceArea(company, "400 W Market St, Greensboro, NC")).isFalse();
    }

    @Test
    void propertyIsInServiceArea_returnsTrue_whenAtCenterLocation() {
        // Exactly at the company's service area center - 0 miles
        when(googleMapsService.geocode("101 S Tryon St, Charlotte, NC"))
                .thenReturn(new LatLng(35.2271, -80.8431));

        assertThat(geoUtils.propertyIsInServiceArea(company, "101 S Tryon St, Charlotte, NC")).isTrue();
    }

    @Test
    void propertyIsInServiceArea_returnsTrue_whenJustInsideRadius() {
        // Rock Hill, SC - ~23 miles south of Charlotte
        when(googleMapsService.geocode("330 E Black St, Rock Hill, SC"))
                .thenReturn(new LatLng(34.9249, -81.0251));

        assertThat(geoUtils.propertyIsInServiceArea(company, "330 E Black St, Rock Hill, SC")).isTrue();
    }

    @Test
    void propertyIsInServiceArea_returnsTrue_whenGeocodingFails() {
        when(googleMapsService.geocode("not a real address"))
                .thenThrow(new GeocodingException("not a real address"));

        assertThat(geoUtils.propertyIsInServiceArea(company, "not a real address")).isTrue();
    }
}
