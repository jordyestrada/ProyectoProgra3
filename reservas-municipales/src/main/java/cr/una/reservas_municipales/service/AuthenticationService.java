package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AzureAdService azureAdService;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            log.debug("Processing authentication request for email: {}", loginRequest.getEmail());
            log.debug("Azure token present: {}", loginRequest.getAzureToken() != null && !loginRequest.getAzureToken().isEmpty());
            log.debug("Email and password present: {}", loginRequest.getEmail() != null && loginRequest.getPassword() != null);
            
            if (loginRequest.getAzureToken() != null && !loginRequest.getAzureToken().isEmpty()) {
                log.debug("Attempting Azure AD authentication");
                return authenticateWithAzureAD(loginRequest.getAzureToken());
            }
            
            if (loginRequest.getEmail() != null && loginRequest.getPassword() != null) {
                log.debug("Attempting local credentials authentication");
                return authenticateWithLocalCredentials(loginRequest.getEmail(), loginRequest.getPassword());
            }
            
            log.debug("No valid authentication method provided - Email: {}, Password: {}, Azure Token: {}", 
                loginRequest.getEmail(), 
                loginRequest.getPassword() != null ? "[PRESENT]" : "[NULL]",
                loginRequest.getAzureToken() != null ? "[PRESENT]" : "[NULL]");
            throw new BadCredentialsException("No valid authentication method provided");
            
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    private JwtResponse authenticateWithAzureAD(String azureToken) {
        if (!azureAdService.validateAzureToken(azureToken)) {
            throw new BadCredentialsException("Invalid Azure AD token");
        }
        AzureAdService.AzureUserInfo azureUserInfo = azureAdService.getUserInfoFromToken(azureToken);
        if (azureUserInfo == null || azureUserInfo.getEmail() == null) {
            throw new BadCredentialsException("Could not retrieve user information from Azure AD");
        }

        User user = findOrCreateUser(azureUserInfo);
        String jwtToken = jwtService.generateToken(user.getEmail(), "ROLE_" + user.getRole().getCode());
        
        return new JwtResponse(
            jwtToken,
            user.getFullName(),
            user.getEmail(),
            user.getRole().getCode(),
            86400000L // 24 hours
        );
    }

    private JwtResponse authenticateWithLocalCredentials(String email, String password) {
        log.warn("Using local authentication - not recommended for production");
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        
        User user = userOpt.get();
        
        if (!"testpass".equals(password)) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        String jwtToken = jwtService.generateToken(user.getEmail(), "ROLE_" + user.getRole().getCode());
        
        return new JwtResponse(
            jwtToken,
            user.getFullName(),
            user.getEmail(),
            user.getRole().getCode(),
            86400000L // 24 hours
        );
    }

    private User findOrCreateUser(AzureAdService.AzureUserInfo azureUserInfo) {
        Optional<User> existingUser = userRepository.findByEmail(azureUserInfo.getEmail());
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // Create new user from Azure AD info
        // Note: You'll need to implement proper role assignment logic
        log.info("Creating new user from Azure AD: {}", azureUserInfo.getEmail());
        
        // For now, assign default role - you should implement proper role logic
        throw new RuntimeException("User not found in local database. Please contact administrator to create your account.");
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.getUsernameFromToken(token);
    }
}