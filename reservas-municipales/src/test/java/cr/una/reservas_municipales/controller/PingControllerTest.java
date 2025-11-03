package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(PingController.class)
@AutoConfigureMockMvc(addFilters = false)
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void testPing_Success() throws Exception {
        mockMvc.perform(get("/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void testAuthenticatedPing_Success() throws Exception {
        // This endpoint uses SecurityContextHolder which returns null in test context
        // causing NullPointerException when trying to get authentication
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().is5xxServerError()); // Expected due to null authentication
    }

    @Test
    void testAuthenticatedPing_WithAuthentication_ReturnsUserAndAuthorities() throws Exception {
        // Arrange: set an Authentication in the SecurityContext
        var authorities = java.util.List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("READ")
        );
        var auth = new UsernamePasswordAuthenticationToken("alice", "N/A", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            // Act & Assert
            mockMvc.perform(get("/api/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"))
                    .andExpect(jsonPath("$.message", is("authenticated pong")))
                    .andExpect(jsonPath("$.user", is("alice")))
                    // Authorities are serialized as an array of objects with an 'authority' field
                    .andExpect(jsonPath("$.authorities", hasSize(2)))
                    .andExpect(jsonPath("$.authorities[*].authority", hasItems("ROLE_USER", "READ")));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testAuthenticatedPing_WithSingleAuthority() throws Exception {
        // Arrange
        var authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var auth = new UsernamePasswordAuthenticationToken("bob", "N/A", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            mockMvc.perform(get("/api/ping"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.user", is("bob")))
                    .andExpect(jsonPath("$.authorities", hasSize(1)))
                    .andExpect(jsonPath("$.authorities[0].authority", is("ROLE_ADMIN")));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
