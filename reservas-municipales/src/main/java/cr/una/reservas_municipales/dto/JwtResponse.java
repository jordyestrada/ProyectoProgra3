package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;
    private String roleCode;
    private Long expiresIn;

    public JwtResponse(String token, String username, String email, String roleCode, Long expiresIn) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.roleCode = roleCode;
        this.expiresIn = expiresIn;
    }
}