package com.pricetag.backend.service;

import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.entity.Property;
import com.pricetag.backend.process.LookupResult;
import org.springframework.stereotype.Service;

@Service
public class PropertyService {

    public Property createProperty(String formattedAddress,
                                   QuoteRequest quoteRequest,
                                   LookupResult lookupResult) {

        Property property = Property.builder()
                .fullAddress(formattedAddress)
                .street(quoteRequest.street())
                .city(quoteRequest.city())
                .state(quoteRequest.state())
                .zip(quoteRequest.zip())
                .sqft(lookupResult.data().sqft())
                .numberOfStories(lookupResult.data().stories())
                .yearBuilt(lookupResult.data().yearBuilt())
                .garageSizeCars(lookupResult.data().garage())
                .propertyType(lookupResult.data().propertyType())
                .build();

        return property;

    }

}
