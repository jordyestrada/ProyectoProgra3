package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@Profile({"dev", "docker"})
@RequiredArgsConstructor
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeTestUsers();
    }

    private void initializeRoles() {
        createRoleIfNotExists("ADMIN", "Administrador del sistema");
        createRoleIfNotExists("SUPERVISOR", "Supervisor de espacios");
        createRoleIfNotExists("USER", "Usuario común");
    }

    private void createRoleIfNotExists(String code, String name) {
        if (roleRepository.findById(code).isEmpty()) {
            Role role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setCreatedAt(OffsetDateTime.now());
            roleRepository.save(role);
            log.info("Created role: {}", code);
        }
    }

    private void initializeTestUsers() {
        createUserIfNotExists("admin@test.com", "Administrador Test", "ADMIN");
        createUserIfNotExists("supervisor@test.com", "Supervisor Test", "SUPERVISOR");
        createUserIfNotExists("user@test.com", "Usuario Test", "USER");
    }

    private void createUserIfNotExists(String email, String fullName, String roleCode) {
        if (userRepository.findByEmail(email).isEmpty()) {
            Role role = roleRepository.findById(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));

            User user = new User();
            user.setUserId(UUID.randomUUID());
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setActive(true);
            user.setCreatedAt(OffsetDateTime.now());
            user.setUpdatedAt(OffsetDateTime.now());

            userRepository.save(user);
            log.info("Created test user: {} with role: {}", email, roleCode);
        }
    }
}