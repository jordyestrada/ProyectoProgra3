package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.notification.NotificationEvent;
import cr.una.reservas_municipales.notification.NotificationSender;
import cr.una.reservas_municipales.notification.NotificationType;
import cr.una.reservas_municipales.notification.NotificationSender;
import cr.una.reservas_municipales.repository.RoleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private User testUser;
    private Role testRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testRole = new Role();
        testRole.setCode("USER");
        testRole.setName("Usuario");

    adminRole = new Role();
    adminRole.setCode("ADMIN");
    adminRole.setName("Administrador");

        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setEmail("test@test.com");
        testUser.setFullName("Test User");
        testUser.setPhone("88888888");
        testUser.setActive(true);
        testUser.setRole(testRole);
    }

    @Test
    void testListAll_Success() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setUserId(UUID.randomUUID());
        anotherUser.setEmail("another@test.com");
        anotherUser.setFullName("Another User");
        anotherUser.setActive(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));

        // Act
        List<UserDto> result = userService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());
        assertEquals("another@test.com", result.get(1).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testListAll_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<UserDto> result = userService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetById_Success() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDto> result = userService.getById(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUserId, result.get().getUserId());
        assertEquals("test@test.com", result.get().getEmail());
        assertEquals("Test User", result.get().getFullName());
        assertEquals("88888888", result.get().getPhone());
        assertTrue(result.get().isActive());
        assertEquals("USER", result.get().getRoleCode());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testGetById_NotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act
        Optional<UserDto> result = userService.getById(testUserId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testGetById_UserWithoutRole() {
        // Arrange
        testUser.setRole(null);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDto> result = userService.getById(testUserId);

        // Assert
        assertTrue(result.isPresent());
        assertNull(result.get().getRoleCode());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testToDto_WithAllFields() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserDto> result = userService.getById(testUserId);

        // Assert
        assertTrue(result.isPresent());
        UserDto dto = result.get();
        assertEquals(testUserId, dto.getUserId());
        assertEquals("test@test.com", dto.getEmail());
        assertEquals("Test User", dto.getFullName());
        assertEquals("88888888", dto.getPhone());
        assertTrue(dto.isActive());
        assertEquals("USER", dto.getRoleCode());
    }

    // ================= changeUserRole =================
    @Test
    void testChangeUserRole_Success_SendsNotification() {
        // Arrange
        UUID uid = testUserId;
        when(userRepository.findById(uid)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDto result = userService.changeUserRole(uid, "ADMIN");

        // Assert
        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleCode());
        verify(userRepository).save(argThat(u -> u.getRole() != null && "ADMIN".equals(u.getRole().getCode())));

        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationSender, times(1)).send(captor.capture());
        NotificationEvent event = captor.getValue();
        assertEquals(NotificationType.USER_ROLE_CHANGED, event.getType());
        assertEquals(testUserId, event.getUserId());
        assertEquals(testUser.getEmail(), event.getEmail());
        assertNotNull(event.getOccurredAt());
        assertNotNull(event.getData());
        assertEquals("Test User", event.getData().get("userName"));
        assertEquals("USER", event.getData().get("oldRole"));
        assertEquals("ADMIN", event.getData().get("newRole"));
        assertEquals("Administrador", event.getData().get("newRoleName"));
    }
    @Test
    void testChangeUserRole_Success() {
        // Arrange
        UUID userId = testUser.getUserId();
        String newRoleCode = "ADMIN";
        
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator role");
        adminRole.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(newRoleCode)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(notificationSender).send(any());

        // Act
        UserDto result = userService.changeUserRole(userId, newRoleCode);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findById(newRoleCode);
        verify(userRepository, times(1)).save(testUser);
        verify(notificationSender, times(1)).send(any());
        assertEquals(adminRole, testUser.getRole());
    }

    @Test
    void testChangeUserRole_WithROLE_Prefix() {
        // Arrange
        UUID userId = testUser.getUserId();
        String newRoleCodeWithPrefix = "ROLE_SUPERVISOR";
        String normalizedRoleCode = "SUPERVISOR";
        
        Role supervisorRole = new Role();
        supervisorRole.setCode("SUPERVISOR");
        supervisorRole.setName("Supervisor role");
        supervisorRole.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(normalizedRoleCode)).thenReturn(Optional.of(supervisorRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(notificationSender).send(any());

        // Act
        UserDto result = userService.changeUserRole(userId, newRoleCodeWithPrefix);

        // Assert
        assertNotNull(result);
        verify(roleRepository, times(1)).findById(normalizedRoleCode);
        assertEquals(supervisorRole, testUser.getRole());
    }

    @Test
    void testChangeUserRole_UserNotFound() {
        UUID uid = UUID.randomUUID();
        when(userRepository.findById(uid)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changeUserRole(uid, "ADMIN"));
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
        verify(notificationSender, never()).send(any());
        verify(userRepository, never()).save(any());
        // Arrange
        UUID userId = UUID.randomUUID();
        String newRoleCode = "ADMIN";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserRole(userId, newRoleCode);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(notificationSender, never()).send(any());
    }

    @Test
    void testChangeUserRole_RoleNotFound() {
        UUID uid = testUserId;
        when(userRepository.findById(uid)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changeUserRole(uid, "ADMIN"));
        assertTrue(ex.getMessage().contains("Rol no encontrado"));
        verify(notificationSender, never()).send(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeUserRole_SameRole_Throws() {
        UUID uid = testUserId;
        when(userRepository.findById(uid)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("USER")).thenReturn(Optional.of(testRole));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.changeUserRole(uid, "USER"));
        assertTrue(ex.getMessage().contains("ya tiene el rol"));
        verify(notificationSender, never()).send(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeUserRole_NotificationThrows_NoRollback() {
        UUID uid = testUserId;
        when(userRepository.findById(uid)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("smtp down")).when(notificationSender).send(any(NotificationEvent.class));

        UserDto result = userService.changeUserRole(uid, "ADMIN");

        assertNotNull(result);
        assertEquals("ADMIN", result.getRoleCode());
        verify(userRepository, times(1)).save(any(User.class));
        verify(notificationSender, times(1)).send(any(NotificationEvent.class));
    }

    // listAll: user with null role => roleCode null
    @Test
    void testListAll_UserWithNullRole_RoleCodeNull() {
        User u = new User();
        u.setUserId(UUID.randomUUID());
        u.setEmail("norole@test.com");
        u.setFullName("No Role");
        u.setActive(true);
        u.setRole(null);

        when(userRepository.findAll()).thenReturn(java.util.List.of(u));
        java.util.List<UserDto> result = userService.listAll();
        assertEquals(1, result.size());
        assertNull(result.get(0).getRoleCode());

        // Arrange
        UUID userId = testUser.getUserId();
        String newRoleCode = "INVALID_ROLE";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(newRoleCode)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserRole(userId, newRoleCode);
        });

        assertEquals("Rol no encontrado: " + newRoleCode, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findById(newRoleCode);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationSender, never()).send(any());
    }

    @Test
    void testChangeUserRole_SameRole() {
        // Arrange
        UUID userId = testUser.getUserId();
        String currentRoleCode = testRole.getCode(); // "USER"

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(currentRoleCode)).thenReturn(Optional.of(testRole));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.changeUserRole(userId, currentRoleCode);
        });

        assertTrue(exception.getMessage().contains("ya tiene el rol"));
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findById(currentRoleCode);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationSender, never()).send(any());
    }

    @Test
    void testChangeUserRole_EmailFailureDoesNotAffectTransaction() {
        // Arrange
        UUID userId = testUser.getUserId();
        String newRoleCode = "ADMIN";
        
        Role adminRole = new Role();
        adminRole.setCode("ADMIN");
        adminRole.setName("Administrator role");
        adminRole.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(newRoleCode)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doThrow(new RuntimeException("Email service error")).when(notificationSender).send(any());

        // Act - Should not throw exception even if email fails
        UserDto result = userService.changeUserRole(userId, newRoleCode);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userRepository, times(1)).save(testUser);
        verify(notificationSender, times(1)).send(any());
        assertEquals(adminRole, testUser.getRole());
    }
}
