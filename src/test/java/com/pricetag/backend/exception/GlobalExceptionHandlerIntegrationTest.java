package com.pricetag.backend.exception;

import com.pricetag.backend.security.JwtAuthEntryPoint;
import com.pricetag.backend.security.JwtAuthFilter;
import com.pricetag.backend.security.JwtService;
import com.pricetag.backend.security.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ThrowingTestController.class)
@Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        JwtAuthEntryPoint.class,
        GlobalExceptionHandler.class
})
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    private void mockAuthenticatedUser() {
        UserDetails userDetails = User.withUsername("test@example.com")
                .password("")
                .authorities(Collections.emptyList())
                .build();
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtService.isTokenValid(anyString(), any(UserDetails.class))).thenReturn(true);
    }

    @Test
    void runtimeExceptionFromAuthenticatedRequestReturns500() throws Exception {
        mockAuthenticatedUser();

        mockMvc.perform(get("/test/runtime-exception")
                        .cookie(new Cookie("auth_token", "fake-token")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    @Test
    void nullPointerExceptionFromAuthenticatedRequestReturns500() throws Exception {
        mockAuthenticatedUser();

        mockMvc.perform(get("/test/null-pointer")
                        .cookie(new Cookie("auth_token", "fake-token")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.statusCode").value(500));
    }
}
