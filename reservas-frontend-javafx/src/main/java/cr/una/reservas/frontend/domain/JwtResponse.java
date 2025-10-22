package cr.una.reservas.frontend.domain;

import lombok.Data;
import java.util.List;

/**
 * DTO para JWT Response (respuesta de login)
 */
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
    private List<String> roles;
}
