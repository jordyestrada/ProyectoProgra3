package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private boolean active;
    private String roleCode;
}
