package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.JwtResponse;
import cr.una.reservas_municipales.dto.LoginRequest;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AzureAdService azureAdService;

    private static final String CLIENT_ID = "f36e1d71-202a-4e09-bea4-dc15ce84bec2";
    private static final String JWKS_URL = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
    private final String jwtSecret = "myDevSecretKey123456789012345678901234567890";

    public static record AzureProfile(String email, String name, String oid, String tid) {}

    private AzureProfile validateAzureIdToken(String idToken) {
        try {
            // Valida firma RS256 y tiempos con el decodificador de Spring
            var decoder = buildAzureJwtDecoder();

            Jwt jwt = decoder.decode(idToken);

            // aud puede ser String o lista según proveedor/token
            String aud = stringOrFirst(jwt.getClaim("aud"));
            if (!CLIENT_ID.equals(aud)) throw new SecurityException("aud inválido: " + aud);

            // Acepta consumers/organizations/tenant, pero exige host + sufijo /v2.0
            String iss = jwt.getClaimAsString("iss");
            if (iss == null || !iss.startsWith("https://login.microsoftonline.com/") || !iss.endsWith("/v2.0")) {
                throw new SecurityException("iss inválido: " + iss);
            }

            // email puede ser "email" (string), "preferred_username" (string) o "emails" (array)
            String email = firstNonEmpty(
                    jwt.getClaimAsString("email"),
                    jwt.getClaimAsString("preferred_username"),
                    claimFirstFromArray(jwt, "emails")
            );
            if (email == null || email.isBlank()) throw new IllegalStateException("El token no trae email");

            // name puede faltar; usa email como fallback
            String name = firstNonEmpty(
                    jwt.getClaimAsString("name"),
                    jwt.getClaimAsString("given_name"),
                    email
            );

            // oid/sub y tid como strings
            String oid = firstNonEmpty(jwt.getClaimAsString("oid"), jwt.getClaimAsString("sub"));
            String tid = firstNonEmpty(jwt.getClaimAsString("tid"), "common");

            return new AzureProfile(email, name, oid, tid);
        } catch (Exception ex) {
            // Mantén este mensaje para que el front muestre el detalle
            throw new RuntimeException("Azure authentication failed: " + ex.getMessage(), ex);
        }
    }

    // Extracted for testability: allows tests to stub the decoder and simulate valid JWTs without network
    protected JwtDecoder buildAzureJwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(JWKS_URL)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
    }

    // Helpers tolerantes a arrays/listas
    private static String stringOrFirst(Object v) {
        if (v == null) return null;
        if (v instanceof String s) return s;
        if (v instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            return first != null ? first.toString() : null;
        }
        return v.toString();
    }

    private static String claimFirstFromArray(Jwt jwt, String key) {
        Object v = jwt.getClaim(key);
        if (v instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            return first != null ? first.toString() : null;
        }
        return null;
    }

    private static String firstNonEmpty(String... vals) {
        if (vals == null) return null;
        for (String s : vals) if (s != null && !s.isBlank()) return s;
        return null;
    }

    public JwtResponse loginWithAzure(String idToken) {
        try {
            AzureProfile profile = validateAzureIdToken(idToken);

            User user = findOrCreateUserByEmail(profile.email(), profile.name());
            UUID userId = user.getUserId();
            List<String> roles = List.of("ROLE_" + user.getRole().getCode());

            var key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            String myJwt = Jwts.builder()
                    .subject(userId.toString())
                    .claim("email", profile.email())
                    .claim("name", profile.name())
                    .claim("roles", roles)
                    .issuedAt(new Date())
                    .expiration(Date.from(Instant.now().plusSeconds(7200)))
                    .signWith(key)
                    .compact();

            return new JwtResponse(
                myJwt,
                user.getFullName(),
                user.getEmail(),
                user.getRole().getCode(),
                7200000L
            );
        } catch (Exception e) {
            log.error("Azure login failed: {}", e.getMessage());
            throw new BadCredentialsException("Azure authentication failed: " + e.getMessage());
        }
    }

    private User findOrCreateUserByEmail(String email, String name) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> {
                // Validación de dominio permitido
                if (!email.toLowerCase().endsWith("@gmail.com") && 
                    !email.toLowerCase().endsWith("@estudiantec.cr") &&
                    !email.toLowerCase().endsWith("@itcr.ac.cr")) {
                    throw new SecurityException("Dominio de email no permitido: " + email);
                }
                
                log.info("Creating new user from Azure AD: {}", email);
                
                // Buscar rol USER (debe existir en BD)
                Role userRole = roleRepository.findById("USER")
                    .orElseThrow(() -> new IllegalStateException("Rol 'USER' no existe en la base de datos"));
                
                User newUser = new User();
                newUser.setUserId(UUID.randomUUID());
                newUser.setEmail(email);
                newUser.setFullName(name != null && !name.isBlank() ? name : email);
                newUser.setActive(true);
                newUser.setRole(userRole);
                newUser.setCreatedAt(OffsetDateTime.now());
                newUser.setUpdatedAt(OffsetDateTime.now());
                
                return userRepository.save(newUser);
            });
    }

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        try {
            log.debug("Processing authentication request for email: {}", loginRequest.getEmail());
            log.debug("Azure token present: {}", loginRequest.getAzureToken() != null && !loginRequest.getAzureToken().isEmpty());
            log.debug("Email and password present: {}", loginRequest.getEmail() != null && loginRequest.getPassword() != null);
            // Extra visibility using explicit labels as requested
            log.debug("Azure Token label: {}", loginRequest.getAzureToken() != null ? "[PRESENT]" : "[NULL]");
            
            if (loginRequest.getAzureToken() != null && !loginRequest.getAzureToken().isEmpty()) {
                log.debug("Attempting Azure AD authentication");
                return loginWithAzure(loginRequest.getAzureToken());
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
            86400000L
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

    /**
     * Returns current user info payload for /api/auth/me endpoint.
     * Kept simple for now; can be extended later to include real user details.
     */
    public java.util.Map<String, String> currentUserInfo() {
        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "User info endpoint - to be implemented");
        return response;
    }
}