package com.pricetag.backend.util;

import com.pricetag.backend.dto.LatLng;
import com.pricetag.backend.entity.Company;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GeoUtilsTest {

    private Company company;

    @BeforeEach
    void setup() {
        company = new Company();
        company.setServiceRadiusMiles(30);
        company.setServiceAreaLatitude(35.2271); // Charlotte
        company.setServiceAreaLongitude(-80.8431);
    }

    @Test
    void givenAddressWithinServiceArea_whenIsInServiceArea_thenReturnsTrue() {
        LatLng davidson = new LatLng(35.4791, -80.8193);
        assertThat(GeoUtils.isInServiceArea(company, davidson)).isTrue();
    }

    @Test
    void givenAddressOutsideServiceArea_whenIsInServiceArea_thenReturnsFalse() {
        LatLng greensboro = new LatLng(36.0726, -79.7920);
        assertThat(GeoUtils.isInServiceArea(company, greensboro)).isFalse();
    }
}
