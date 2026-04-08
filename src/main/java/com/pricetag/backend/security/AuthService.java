package com.pricetag.backend.security;

import com.pricetag.backend.dto.auth.AuthRequest;
import com.pricetag.backend.dto.auth.RegistrationRequest;
import com.pricetag.backend.dto.auth.UserResponse;
import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.User;
import com.pricetag.backend.exception.EmailAlreadyExistsException;
import com.pricetag.backend.repository.CompanyRepository;
import com.pricetag.backend.repository.UserRepository;
import com.pricetag.backend.util.SlugGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SlugGenerator slugGenerator;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    @Transactional
    public ResponseCookie register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("User with email " + request.email() + " already exists");
        }
        if (companyRepository.existsByEmail(request.companyEmail())) {
            throw new EmailAlreadyExistsException("Company with email " + request.companyEmail() + " already exists");
        }

        Company company = Company.builder()
                .name(request.companyName())
                .slug(slugGenerator.createSlug(request.companyName()))
                .email(request.companyEmail())
                .phone(request.companyPhone())
                .build();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(User.Role.OWNER)
                .company(company)
                .build();

        companyRepository.save(company);
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return buildCookie(token);
    }

    public ResponseCookie login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);

        return buildCookie(token);
    }

    public ResponseCookie logout() {
        return ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    public UserResponse buildUserResponse(User user) {
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private ResponseCookie buildCookie(String authToken) {
        return ResponseCookie.from("auth_token", authToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(expirationMillis / 1000)
                .sameSite("Strict")
                .build();
    }
}
