package com.pricetag.backend.controller;

import com.pricetag.backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class BaseController {

    @Autowired
    private JwtService jwtService;

    protected UUID extractCompanyId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.extractCompanyId(token);
    }


}
