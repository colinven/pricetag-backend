package com.pricetag.backend.controller;

import com.pricetag.backend.dto.request.PricingConfigurationRequest;
import com.pricetag.backend.dto.request.ServiceAreaConfigurationRequest;
import com.pricetag.backend.dto.response.PricingConfigurationResponse;
import com.pricetag.backend.dto.response.ServiceAreaConfigurationResponse;
import com.pricetag.backend.dto.response.SettingsResponse;
import com.pricetag.backend.security.JwtService;
import com.pricetag.backend.service.SettingsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/company/settings")
@RequiredArgsConstructor
public class SettingsController extends BaseController {

    private final SettingsService settingsService;

    @PutMapping("/pricing-config")
    public ResponseEntity<PricingConfigurationResponse> updatePricingConfig(
            HttpServletRequest request,
            @RequestBody @Valid PricingConfigurationRequest pricingConfigurationRequest) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(settingsService.savePricingConfiguration(companyId, pricingConfigurationRequest));
    }

    @PutMapping("/service-area")
    public ResponseEntity<ServiceAreaConfigurationResponse> updateServiceArea(
            HttpServletRequest request,
            @RequestBody @Valid ServiceAreaConfigurationRequest serviceAreaConfigurationRequest) {

        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(settingsService.saveServiceAreaConfiguration(companyId, serviceAreaConfigurationRequest));
    }

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings(HttpServletRequest request) {
        UUID companyId = extractCompanyId(request);
        return ResponseEntity.ok(settingsService.getAllSettings(companyId));
    }

}
