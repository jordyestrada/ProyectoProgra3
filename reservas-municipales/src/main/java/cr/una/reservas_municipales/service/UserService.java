package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public List<UserDto> listAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<UserDto> getById(UUID id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public Optional<UserDto> promoteToAdmin(UUID userId) {
        log.info("Promoting user {} to ADMIN role", userId);
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            log.warn("User {} not found for promotion", userId);
            return Optional.empty();
        }
        
        Role adminRole = roleRepository.findById("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found in database"));
        
        User user = userOpt.get();
        user.setRole(adminRole);
        user.setUpdatedAt(OffsetDateTime.now());
        
        User saved = userRepository.save(user);
        log.info("User {} successfully promoted to ADMIN", userId);
        
        return Optional.of(toDto(saved));
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

