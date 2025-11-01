package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.model.Role;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testRole = new Role();
        testRole.setCode("USER");
        testRole.setName("Usuario");

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
}
