package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody String rawBody, HttpServletRequest request) {
        try {
            log.debug("Raw request body: {}", rawBody);
            log.debug("Content-Type header: {}", request.getHeader("Content-Type"));
            
            ObjectMapper mapper = new ObjectMapper();
            LoginRequest loginRequest = mapper.readValue(rawBody, LoginRequest.class);
            
            log.debug("Parsed login request - Email: {}, Password provided: {}, Azure token provided: {}", 
                loginRequest.getEmail(), 
                loginRequest.getPassword() != null ? "YES" : "NO",
                loginRequest.getAzureToken() != null ? "YES" : "NO");
            
            if (loginRequest.getAzureToken() != null && !loginRequest.getAzureToken().isBlank()) {
                return ResponseEntity.ok(authenticationService.loginWithAzure(loginRequest.getAzureToken()));
            }
            
            JwtResponse jwtResponse = authenticationService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid JSON format");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (BadCredentialsException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error");
            error.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authenticationService.validateToken(token);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                String username = authenticationService.getUsernameFromToken(token);
                response.put("username", username);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User info endpoint - to be implemented");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.badRequest().build();
        }
    }
}