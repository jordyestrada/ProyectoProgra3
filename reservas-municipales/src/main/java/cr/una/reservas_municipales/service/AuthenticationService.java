package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AzureAdService azureAdService;

    @Value("${app.auth.azure.auto-provision:false}")
    private boolean autoProvision;

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

    @Transactional
    private User findOrCreateUser(AzureAdService.AzureUserInfo azureUserInfo) {
        final String email = normalizeEmail(azureUserInfo.getEmail());
        final String fullName = coalesce(azureUserInfo.getName(), azureUserInfo.getPreferredUsername(), email);
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.isActive()) {
                throw new BadCredentialsException("User is disabled. Contact administrator.");
            }
            return user;
        }
        
        // Usuario no existe
        if (!autoProvision) {
            log.error("Authentication failed: User not found in local database. Auto-provision disabled.");
            throw new BadCredentialsException(
                "Authentication failed: User not found in local database. Please contact administrator to create your account."
            );
        }
        
        // Auto-provisioning activado
        log.info("Auto-provisioning new user from Azure AD: {}", email);
        
        User newUser = new User();
        newUser.setUserId(UUID.randomUUID());
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setActive(true);
        newUser.setPasswordHash(null);
        newUser.setPhone(null);
        
        // Asignar rol USER por defecto
        Role userRole = roleRepository.findById("USER")
            .orElseThrow(() -> new RuntimeException("Default role USER not found in database"));
        newUser.setRole(userRole);
        
        OffsetDateTime now = OffsetDateTime.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);
        
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException dup) {
            // En caso de carrera: alguien lo creó a la vez → recupéralo
            return userRepository.findByEmail(email)
                .orElseThrow(() -> dup);
        }
    }
    
    private static String normalizeEmail(String e) {
        return e == null ? null : e.trim().toLowerCase();
    }
    
    private static String coalesce(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    public boolean validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsernameFromToken(String token) {
        return jwtService.getUsernameFromToken(token);
    }
}