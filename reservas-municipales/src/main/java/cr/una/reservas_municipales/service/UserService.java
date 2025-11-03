package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.notification.NotificationEvent;
import cr.una.reservas_municipales.notification.NotificationSender;
import cr.una.reservas_municipales.notification.NotificationType;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationSender notificationSender;

    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<UserDto> getById(UUID id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public UserDto changeUserRole(UUID userId, String newRoleCode) {
        log.info("Intentando cambiar rol del usuario {} a {}", userId, newRoleCode);
        
        // Normalizar el código del rol: remover prefijo "ROLE_" si existe
        // La BD tiene los roles como: ADMIN, SUPERVISOR, USER (sin ROLE_)
        String normalizedRoleCode = newRoleCode.replace("ROLE_", "");
        
        // Buscar el usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        
        // Buscar el nuevo rol
        Role newRole = roleRepository.findById(normalizedRoleCode)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + normalizedRoleCode));
        
        // Guardar el rol anterior para el log y la notificación
        String oldRoleCode = user.getRole().getCode();
        String oldRoleName = user.getRole().getName();
        
        // Verificar si el rol es diferente
        if (oldRoleCode.equals(normalizedRoleCode)) {
            throw new RuntimeException("El usuario ya tiene el rol: " + normalizedRoleCode);
        }
        
        // Actualizar el rol
        user.setRole(newRole);
        user.setUpdatedAt(OffsetDateTime.now());
        User updatedUser = userRepository.save(user);
        
        log.info("Rol del usuario {} cambiado de {} a {}", 
                user.getEmail(), oldRoleCode, normalizedRoleCode);
        
        // Enviar notificación por correo
        try {
            NotificationEvent event = NotificationEvent.builder()
                    .type(NotificationType.USER_ROLE_CHANGED)
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .occurredAt(OffsetDateTime.now())
                    .data(Map.of(
                            "userName", user.getFullName(),
                            "oldRole", oldRoleCode,
                            "newRole", newRoleCode,
                            "newRoleName", newRole.getName()
                    ))
                    .build();
            
            notificationSender.send(event);
            log.info("Notificación de cambio de rol enviada a {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar notificación de cambio de rol: {}", e.getMessage());
            // No lanzamos la excepción para no revertir la transacción
        }
        
        return toDto(updatedUser);
    }

    private UserDto toDto(User u) {
        UserDto d = new UserDto();
        d.setUserId(u.getUserId());
        d.setEmail(u.getEmail());
        d.setFullName(u.getFullName());
        d.setPhone(u.getPhone());
        d.setActive(u.isActive());
        if (u.getRole() != null) d.setRoleCode(u.getRole().getCode());
        return d;
    }
}

