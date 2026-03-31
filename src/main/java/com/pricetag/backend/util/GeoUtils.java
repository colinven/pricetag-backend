package com.pricetag.backend.util;

import com.pricetag.backend.dto.LatLng;
import com.pricetag.backend.entity.Company;
import org.springframework.stereotype.Component;

@Component
public class GeoUtils {

    public static double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int EARTH_RADIUS_MILES = 3956;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c =  2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_MILES * c;
    }

    public static boolean isInServiceArea(Company company, LatLng coordinates) {
        double distanceMiles = haversine(
                company.getServiceAreaLatitude(),
                company.getServiceAreaLongitude(),
                coordinates.lat(),
                coordinates.lng()
        );
        return distanceMiles <= company.getServiceRadiusMiles();
    }
}
