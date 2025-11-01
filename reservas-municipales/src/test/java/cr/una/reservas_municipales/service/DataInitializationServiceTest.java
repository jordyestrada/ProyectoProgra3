package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.SpaceType;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.SpaceTypeRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private SpaceTypeRepository spaceTypeRepository;

    @InjectMocks
    private DataInitializationService dataInitializationService;

    @BeforeEach
    void setUp() {
        // Setup básico
    }

    @Test
    void testRun_InitializesAllData() throws Exception {
        // Arrange
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrador del sistema");
        
        Role supervisorRole = new Role();
        supervisorRole.setCode("SUPERVISOR");
        supervisorRole.setName("Supervisor de espacios");
        
        Role userRole = new Role();
        userRole.setCode("USER");
        userRole.setName("Usuario común");
        
        // Primero los roles no existen
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        
        // Al guardar, devolvemos los roles
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            // Después de guardar, los roles están disponibles
            if ("ADMIN".equals(role.getCode())) {
                when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(adminRole));
            } else if ("SUPERVISOR".equals(role.getCode())) {
                when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.of(supervisorRole));
            } else if ("USER".equals(role.getCode())) {
                when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
            }
            return role;
        });
        
        when(spaceTypeRepository.findById(anyShort())).thenReturn(Optional.empty());
        when(spaceTypeRepository.save(any(SpaceType.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        dataInitializationService.run();

        // Assert
        verify(roleRepository, atLeast(3)).save(any(Role.class)); // ADMIN, SUPERVISOR, USER
        verify(spaceTypeRepository, atLeast(3)).save(any(SpaceType.class)); // 3 tipos de espacios
    }

    @Test
    void testRun_SkipsExistingRoles() throws Exception {
        // Arrange
        Role existingRole = new Role();
        existingRole.setCode("ADMIN");
        existingRole.setName("Administrador");
        
        Role supervisorRole = new Role();
        supervisorRole.setCode("SUPERVISOR");
        supervisorRole.setName("Supervisor de espacios");
        
        Role userRole = new Role();
        userRole.setCode("USER");
        userRole.setName("Usuario común");
        
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(existingRole));
        when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            if ("SUPERVISOR".equals(role.getCode())) {
                when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.of(supervisorRole));
            } else if ("USER".equals(role.getCode())) {
                when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
            }
            return role;
        });
        
        when(spaceTypeRepository.findById(anyShort())).thenReturn(Optional.empty());
        when(spaceTypeRepository.save(any(SpaceType.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        dataInitializationService.run();

        // Assert
        // Solo se deben guardar 2 roles nuevos (SUPERVISOR y USER), no ADMIN
        verify(roleRepository, times(2)).save(any(Role.class));
    }

    @Test
    void testRun_SkipsExistingSpaceTypes() throws Exception {
        // Arrange
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrador del sistema");
        
        Role supervisorRole = new Role();
        supervisorRole.setCode("SUPERVISOR");
        supervisorRole.setName("Supervisor de espacios");
        
        Role userRole = new Role();
        userRole.setCode("USER");
        userRole.setName("Usuario común");
        
        SpaceType existingType = new SpaceType();
        existingType.setSpaceTypeId((short) 1);
        existingType.setName("Parque");
        
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());
        
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role role = invocation.getArgument(0);
            if ("ADMIN".equals(role.getCode())) {
                when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(adminRole));
            } else if ("SUPERVISOR".equals(role.getCode())) {
                when(roleRepository.findById("SUPERVISOR")).thenReturn(Optional.of(supervisorRole));
            } else if ("USER".equals(role.getCode())) {
                when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
            }
            return role;
        });
        
        when(spaceTypeRepository.findById((short) 1)).thenReturn(Optional.of(existingType));
        when(spaceTypeRepository.findById((short) 2)).thenReturn(Optional.empty());
        when(spaceTypeRepository.findById((short) 3)).thenReturn(Optional.empty());
        when(spaceTypeRepository.save(any(SpaceType.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        dataInitializationService.run();

        // Assert
        // Solo se deben guardar 2 tipos nuevos, no el existente
        verify(spaceTypeRepository, times(2)).save(any(SpaceType.class));
    }
}
