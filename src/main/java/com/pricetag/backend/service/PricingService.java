package com.pricetag.backend.service;

import com.pricetag.backend.dto.response.PropertyData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class PricingService {

    @Value("${pricing.baseSqftPrice}")
    private double baseSqftPrice; //
    @Value("${pricing.storyMultiplier}")
    private double storyMultiplier;
    @Value("${pricing.minimumCharge}")
    private int minimumCharge;
    @Value("${pricing.priceRangeBuffer}")
    private int priceRangeBuffer;

    public Integer[] getPrice(PropertyData propertyData, String lastWash) {

        if (propertyData.sqft() == null || propertyData.stories() == null){
            return null;
        }

        if (!propertyData.propertyType().equals("SINGLE_FAMILY") && !propertyData.propertyType().equals("TOWNHOMES")){
            return null;
        }

        int approxGarageSqft = 400; //if no garage data, default to 400sqft (avg 2 car garage)
        if (propertyData.garage() != null && propertyData.garage() != 0){
            approxGarageSqft = propertyData.garage() * 200;
        }

        int totalApproxSqft = propertyData.sqft() + approxGarageSqft;

        // final price/sqft (accounting for number of stories)
        double finalPricePerSqft = baseSqftPrice + ((propertyData.stories() - 1) * storyMultiplier);

        // base price only accounting for total sq footage & number of stories
        double basePrice = totalApproxSqft * finalPricePerSqft;
        //calculate multiplier for years since built
        int currentYear = LocalDate.now().getYear();
        int yearsSinceBuilt = currentYear - propertyData.yearBuilt();

        //calculate age multiplier
        double ageMultiplier = 1.05;
        if (yearsSinceBuilt < 5) ageMultiplier = 1.0;
        else if (yearsSinceBuilt < 10) ageMultiplier = 1.02;
        else if (yearsSinceBuilt < 20) ageMultiplier = 1.035;

        Map<String, Double> washIntervalMultipliers = Map.of(
                "Never / First time", 1.05,
                "Less than 1 year ago", 1.0,
                "1 - 2 years ago", 1.025,
                "3 - 4 years ago", 1.03,
                "5+ years ago", 1.05,
                "Not sure", 1.05
        );

        double lastWashMultiplier = washIntervalMultipliers.getOrDefault(lastWash, 1.05);

        //calculate final price range
        int finalQuoteMedian = (int) Math.ceil(basePrice * ageMultiplier * lastWashMultiplier);
        int finalQuoteLower = (finalQuoteMedian - priceRangeBuffer) < minimumCharge ?
                minimumCharge : (finalQuoteMedian - priceRangeBuffer);
        int finalQuoteHigher = finalQuoteLower + (priceRangeBuffer * 2);

        return new Integer[]{finalQuoteLower, finalQuoteHigher};
    }
}
