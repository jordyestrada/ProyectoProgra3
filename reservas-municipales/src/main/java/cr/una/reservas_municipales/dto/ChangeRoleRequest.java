package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class ChangeRoleRequest {
    @NotNull(message = "El ID del usuario es requerido")
    private UUID userId;
    
    @NotBlank(message = "El código del rol es requerido")
    @Pattern(regexp = "^(ROLE_ADMIN|ROLE_SUPERVISOR|ROLE_USER)$", 
             message = "El rol debe ser ROLE_ADMIN, ROLE_SUPERVISOR o ROLE_USER")
    private String roleCode;
}
