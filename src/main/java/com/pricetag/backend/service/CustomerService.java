package com.pricetag.backend.service;

import com.pricetag.backend.dto.request.QuoteRequest;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer findOrCreate(QuoteRequest request) {
        Customer customer = customerRepository.findByEmail(request.email())
                .orElse(new Customer());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());

        return customer;
    }
}
