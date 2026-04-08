package com.pricetag.backend.dto.auth;

import com.pricetag.backend.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(
        String firstName,
        String lastName,
        String email,
        User.Role role
) {
}
