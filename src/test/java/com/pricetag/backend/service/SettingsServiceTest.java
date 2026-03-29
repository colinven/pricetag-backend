package com.pricetag.backend.service;

import com.pricetag.backend.dto.request.ServiceAreaConfigurationRequest;
import com.pricetag.backend.dto.response.ServiceAreaConfigurationResponse;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.exception.CompanyNotFoundException;
import com.pricetag.backend.repository.CompanyPricingRepository;
import com.pricetag.backend.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettingsServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyPricingRepository companyPricingRepository;

    @InjectMocks
    private SettingsService settingsService;

    private UUID companyId;
    private Company company;

    @BeforeEach
    void setUp() {
        companyId = UUID.randomUUID();
        company = new Company();
        company.setId(companyId);

    }

    @Test
    void givenValidRequest_whenSaveServiceArea_thenUpdatesCompany() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        ServiceAreaConfigurationRequest req = ServiceAreaConfigurationRequest.builder()
                .serviceAreaLatitude(35.2271)
                .serviceAreaLongitude(-80.8431)
                .serviceRadiusMiles(50)
                .build();

        ServiceAreaConfigurationResponse res = settingsService.saveServiceAreaConfiguration(companyId, req);

        assertThat(res.serviceAreaLatitude()).isEqualTo(35.2271);
        assertThat(res.serviceAreaLongitude()).isEqualTo(-80.8431);
        assertThat(res.serviceRadiusMiles()).isEqualTo(50);

        Mockito.verify(companyRepository, Mockito.times(1)).save(company);

    }

    @Test
    void givenInvalidCompanyId_whenSaveServiceArea_thenThrowsException() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ServiceAreaConfigurationRequest req = ServiceAreaConfigurationRequest.builder()
                .serviceAreaLatitude(35.2271)
                .serviceAreaLongitude(-80.8431)
                .serviceRadiusMiles(50)
                .build();

        assertThatThrownBy(() -> settingsService.saveServiceAreaConfiguration(companyId, req))
                .isInstanceOf(CompanyNotFoundException.class);
    }
}
