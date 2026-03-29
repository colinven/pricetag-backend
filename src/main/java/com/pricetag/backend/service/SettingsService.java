package com.pricetag.backend.service;

import com.pricetag.backend.dto.request.PricingConfigurationRequest;
import com.pricetag.backend.dto.request.ServiceAreaConfigurationRequest;
import com.pricetag.backend.dto.response.PricingConfigurationResponse;
import com.pricetag.backend.dto.response.ServiceAreaConfigurationResponse;
import com.pricetag.backend.dto.response.SettingsResponse;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.CompanyPricing;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.repository.CompanyPricingRepository;
import com.pricetag.backend.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final CompanyRepository companyRepository;
    private final CompanyPricingRepository companyPricingRepository;

    public PricingConfigurationResponse savePricingConfiguration(UUID companyId , PricingConfigurationRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
        CompanyPricing pricing = companyPricingRepository
                .findByCompanyId(companyId)
                .orElse(new CompanyPricing());

        pricing.setCompany(company);
        pricing.setBaseSqftPrice(request.baseSqftPrice());
        pricing.setStoryMultiplier(request.storyMultiplier());
        pricing.setMinimumPrice(request.minimumPrice());
        pricing.setPriceRangeBuffer(request.priceRangeBuffer());
        pricing.setQuoteExpiryDays(request.quoteExpiryDays());

        companyPricingRepository.save(pricing);

        PricingConfigurationResponse response = PricingConfigurationResponse.builder()
                .baseSqftPrice(request.baseSqftPrice())
                .storyMultiplier(request.storyMultiplier())
                .minimumPrice(request.minimumPrice())
                .priceRangeBuffer(request.priceRangeBuffer())
                .quoteExpiryDays(request.quoteExpiryDays())
                .build();

        return response;
    }

    @Transactional
    public ServiceAreaConfigurationResponse saveServiceAreaConfiguration(UUID companyId, ServiceAreaConfigurationRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        company.setServiceAreaLatitude(request.serviceAreaLatitude());
        company.setServiceAreaLongitude(request.serviceAreaLongitude());
        company.setServiceRadiusMiles(request.serviceRadiusMiles());

        companyRepository.save(company);

        ServiceAreaConfigurationResponse response = ServiceAreaConfigurationResponse.builder()
                .serviceAreaLatitude(request.serviceAreaLatitude())
                .serviceAreaLongitude(request.serviceAreaLongitude())
                .serviceRadiusMiles(request.serviceRadiusMiles())
                .build();

        return response;
    }

    public SettingsResponse getAllSettings(UUID companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
        CompanyPricing pricing = companyPricingRepository.findByCompanyId(companyId)
                .orElse(null);

        PricingConfigurationResponse pricingConfigurationResponse = pricing == null ? null :
                PricingConfigurationResponse.builder()
                .baseSqftPrice(pricing.getBaseSqftPrice())
                .storyMultiplier(pricing.getStoryMultiplier())
                .minimumPrice(pricing.getMinimumPrice())
                .priceRangeBuffer(pricing.getPriceRangeBuffer())
                .quoteExpiryDays(pricing.getQuoteExpiryDays())
                .build();

        ServiceAreaConfigurationResponse serviceAreaConfigurationResponse = ServiceAreaConfigurationResponse.builder()
                .serviceAreaLongitude(company.getServiceAreaLongitude())
                .serviceAreaLatitude(company.getServiceAreaLatitude())
                .serviceRadiusMiles(company.getServiceRadiusMiles())
                .build();

        SettingsResponse response = SettingsResponse.builder()
                .serviceArea(serviceAreaConfigurationResponse)
                .pricingConfig(pricingConfigurationResponse)
                .build();

        return response;
    }
}
