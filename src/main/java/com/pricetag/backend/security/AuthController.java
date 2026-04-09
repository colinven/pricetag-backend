package com.pricetag.backend.security;

import com.pricetag.backend.dto.auth.AuthRequest;
import com.pricetag.backend.dto.auth.AuthResponse;
import com.pricetag.backend.dto.auth.RegistrationRequest;
import com.pricetag.backend.dto.auth.UserResponse;
import com.pricetag.backend.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.cookie().toString())
                .body(response.user());
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegistrationRequest registrationRequest) {
        AuthResponse response = authService.register(registrationRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, response.cookie().toString())
                .body(response.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = authService.logout();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.buildUserResponse(user));
    }
}
