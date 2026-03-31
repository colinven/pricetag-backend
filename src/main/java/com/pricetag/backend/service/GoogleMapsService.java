package com.pricetag.backend.service;

import com.pricetag.backend.dto.LatLng;
import com.pricetag.backend.dto.response.GeocodeResponse;
import com.pricetag.backend.exception.GeocodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GoogleMapsService {

    @Value("${keys.googleMapsKey}") private String googleMapsApiKey;

    private final RestClient restClient;

    public GoogleMapsService(RestClient restClient) {
        this.restClient = restClient;
    }

    public LatLng geocode(String address) {

        GeocodeResponse response = restClient
                .get()
                .uri("/maps/api/geocode/json?address={address}&key={key}", address, googleMapsApiKey)
                .retrieve()
                .body(GeocodeResponse.class);

        if (response == null || response.results().isEmpty() || !response.status().equals("OK")) {
            throw new GeocodingException(address);
        }

        return new LatLng(
                response.results().getFirst().geometry().location().lat(),
                response.results().getFirst().geometry().location().lng()
        );
    }
}
