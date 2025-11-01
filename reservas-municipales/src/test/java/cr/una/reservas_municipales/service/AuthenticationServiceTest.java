package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AzureAdService azureAdService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private Role testRole;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setCode("USER");
        testRole.setName("Usuario");

        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setEmail("test@test.com");
        testUser.setFullName("Test User");
        testUser.setActive(true);
        testUser.setRole(testRole);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("testpass");
    }

    @Test
    void testAuthenticateUser_UserExists() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // Act
        JwtResponse response = authenticationService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        verify(userRepository, times(1)).findByEmail("test@test.com");
    }

    @Test
    void testAuthenticateUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticateUser(loginRequest);
        });
    }

    @Test
    void testAuthenticateUser_InactiveUser() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(anyString(), anyString())).thenReturn("mock-jwt-token");

        // Act - Usuario inactivo puede loguearse, solo no es recomendado
        JwtResponse response = authenticationService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
    }
}
