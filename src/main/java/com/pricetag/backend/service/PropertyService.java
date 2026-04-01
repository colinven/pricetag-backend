package com.pricetag.backend.service;

import com.pricetag.backend.dto.AddressInfo;
import com.pricetag.backend.dto.LatLng;
import com.pricetag.backend.dto.response.PropertyData;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Property;
import com.pricetag.backend.exception.GeocodingException;
import com.pricetag.backend.repository.PropertyRepository;
import com.pricetag.backend.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class PropertyService {

    public Property createProperty(String formattedAddress,
                                   AddressInfo addressInfo,
                                   PropertyData propertyData) {

        return Property.builder()
                .fullAddress(formattedAddress)
                .street(addressInfo.street())
                .city(addressInfo.city())
                .state(addressInfo.state())
                .zip(addressInfo.zip())
                .sqft(propertyData.sqft())
                .numberOfStories(propertyData.stories())
                .yearBuilt(propertyData.yearBuilt())
                .garageSizeCars(propertyData.garage())
                .propertyType(propertyData.propertyType())
                .build();

    }

    public PropertyData getDataFromExistingProperty(Property existingProperty) {
        return PropertyData.builder()
                .sqft(existingProperty.getSqft())
                .stories(existingProperty.getNumberOfStories())
                .yearBuilt(existingProperty.getYearBuilt())
                .garage(existingProperty.getGarageSizeCars())
                .propertyType(existingProperty.getPropertyType())
                .build();
    }

}
