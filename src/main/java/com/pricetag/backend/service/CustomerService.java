package com.pricetag.backend.service;

import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.repository.CustomerRepository;
import com.pricetag.backend.util.Formatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer findOrCreate(QuoteRequest request, Company company) {
        Customer customer = customerRepository.findByEmailAndCompanyId(request.email(), company.getId())
                .orElse(new Customer());
        customer.setEmail(request.email());
        customer.setPhone(Formatter.formatPhoneNumber(request.phone()));
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setCompany(company);

        return customer;
    }
}
