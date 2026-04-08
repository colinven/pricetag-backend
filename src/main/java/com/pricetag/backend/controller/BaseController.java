package com.pricetag.backend.controller;

import com.pricetag.backend.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public abstract class BaseController {

    @Autowired
    private JwtService jwtService;

    protected UUID extractCompanyId(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("auth_token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return jwtService.extractCompanyId(token);
    }


}
