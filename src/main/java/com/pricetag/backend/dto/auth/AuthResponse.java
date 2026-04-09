package com.pricetag.backend.dto.auth;

import org.springframework.http.ResponseCookie;

public record AuthResponse(
        ResponseCookie cookie,
        UserResponse user
) {}
