package cr.una.reservas.frontend.domain;

import lombok.Data;

/**
 * DTO para Login Request
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
    private String azureToken;
    
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
