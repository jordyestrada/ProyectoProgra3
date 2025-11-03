package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.ChangeRoleRequest;
import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> listAll() {
        return userService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return userService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserRole(
            @Valid @RequestBody ChangeRoleRequest request,
            Authentication authentication) {
        try {
            String adminEmail = authentication.getName();
            log.info("Admin {} solicitando cambio de rol para usuario {} a {}", 
                    adminEmail, request.getUserId(), request.getRoleCode());
            
            UserDto updatedUser = userService.changeUserRole(
                    request.getUserId(), 
                    request.getRoleCode()
            );
            
            log.info("Cambio de rol exitoso. Usuario: {}, Nuevo rol: {}", 
                    updatedUser.getEmail(), updatedUser.getRoleCode());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Rol actualizado exitosamente",
                    "user", updatedUser
            ));
            
        } catch (RuntimeException e) {
            log.error("Error al cambiar rol: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error al cambiar rol",
                    "message", e.getMessage()
            ));
        }
    }
}
