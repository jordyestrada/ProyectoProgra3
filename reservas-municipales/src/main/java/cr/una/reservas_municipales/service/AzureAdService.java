package cr.una.reservas_municipales.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AzureAdService {

    @Value("${spring.cloud.azure.active-directory.profile.tenant-id:}")
    private String tenantId;

    @Value("${spring.cloud.azure.active-directory.credential.client-id:}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean validateAzureToken(String azureToken) {
        try {
            String url = "https://graph.microsoft.com/v1.0/me";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(azureToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("Error validating Azure token: {}", e.getMessage());
            return false;
        }
    }

    public AzureUserInfo getUserInfoFromToken(String azureToken) {
        try {
            String url = "https://graph.microsoft.com/v1.0/me";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(azureToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userInfo = objectMapper.readTree(response.getBody());
                
                return AzureUserInfo.builder()
                        .email(userInfo.get("mail").asText())
                        .displayName(userInfo.get("displayName").asText())
                        .givenName(userInfo.get("givenName").asText())
                        .surname(userInfo.get("surname").asText())
                        .id(userInfo.get("id").asText())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error getting user info from Azure token: {}", e.getMessage());
        }
        return null;
    }

    public static class AzureUserInfo {
        private String email;
        private String displayName;
        private String givenName;
        private String surname;
        private String id;

        public static AzureUserInfoBuilder builder() {
            return new AzureUserInfoBuilder();
        }

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public String getGivenName() { return givenName; }
        public void setGivenName(String givenName) { this.givenName = givenName; }
        
        public String getSurname() { return surname; }
        public void setSurname(String surname) { this.surname = surname; }
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        // Métodos auxiliares para AuthenticationService
        public String getName() { return displayName; }
        public String getPreferredUsername() { return email; }

        public static class AzureUserInfoBuilder {
            private AzureUserInfo userInfo = new AzureUserInfo();

            public AzureUserInfoBuilder email(String email) {
                userInfo.setEmail(email);
                return this;
            }

            public AzureUserInfoBuilder displayName(String displayName) {
                userInfo.setDisplayName(displayName);
                return this;
            }

            public AzureUserInfoBuilder givenName(String givenName) {
                userInfo.setGivenName(givenName);
                return this;
            }

            public AzureUserInfoBuilder surname(String surname) {
                userInfo.setSurname(surname);
                return this;
            }

            public AzureUserInfoBuilder id(String id) {
                userInfo.setId(id);
                return this;
            }

            public AzureUserInfo build() {
                return userInfo;
            }
        }
    }
}