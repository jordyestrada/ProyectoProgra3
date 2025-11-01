package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.service.AuthenticationService;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        jwtResponse = new JwtResponse();
        jwtResponse.setToken("jwt-token-123");
        jwtResponse.setEmail("test@example.com");
        jwtResponse.setRoleCode("ROLE_USER");
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roleCode").value("ROLE_USER"));

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testLogin_WithAzureToken() throws Exception {
        loginRequest.setAzureToken("azure-token-123");
        loginRequest.setPassword(null);

        when(authenticationService.loginWithAzure(anyString()))
                .thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        verify(authenticationService, times(1)).loginWithAzure("azure-token-123");
        verify(authenticationService, never()).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testLogin_BadCredentials() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").exists());

        verify(authenticationService, times(1)).authenticateUser(any(LoginRequest.class));
    }

    @Test
    void testLogin_InvalidJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid JSON format"));
    }

    @Test
    void testLogin_UnexpectedException() throws Exception {
        when(authenticationService.authenticateUser(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }

    @Test
    void testValidateToken_Valid() throws Exception {
        when(authenticationService.validateToken("valid-token")).thenReturn(true);
        when(authenticationService.getUsernameFromToken("valid-token")).thenReturn("test@example.com");

        mockMvc.perform(post("/api/auth/validate")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("test@example.com"));

        verify(authenticationService, times(1)).validateToken("valid-token");
        verify(authenticationService, times(1)).getUsernameFromToken("valid-token");
    }

    @Test
    void testValidateToken_Invalid() throws Exception {
        when(authenticationService.validateToken("invalid-token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/validate")
                        .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.username").doesNotExist());

        verify(authenticationService, times(1)).validateToken("invalid-token");
        verify(authenticationService, never()).getUsernameFromToken(anyString());
    }

    @Test
    void testValidateToken_Exception() throws Exception {
        when(authenticationService.validateToken(anyString()))
                .thenThrow(new RuntimeException("Token validation error"));

        mockMvc.perform(post("/api/auth/validate")
                        .param("token", "error-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Token validation failed"));
    }

    @Test
    void testGetCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
