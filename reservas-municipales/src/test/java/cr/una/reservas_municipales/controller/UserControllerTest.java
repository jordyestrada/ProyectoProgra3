package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.UserDto;
import cr.una.reservas_municipales.service.UserService;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private UserDto userDto1;
    private UserDto userDto2;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userDto1 = new UserDto();
        userDto1.setUserId(userId);
        userDto1.setEmail("user1@example.com");
        userDto1.setFullName("John Doe");
        userDto1.setPhone("12345678");
        userDto1.setActive(true);
        userDto1.setRoleCode("ROLE_USER");

        userDto2 = new UserDto();
        userDto2.setUserId(UUID.randomUUID());
        userDto2.setEmail("user2@example.com");
        userDto2.setFullName("Jane Smith");
        userDto2.setPhone("87654321");
        userDto2.setActive(true);
        userDto2.setRoleCode("ROLE_USER");
    }

    @Test
    void testListAll_Success() throws Exception {
        List<UserDto> users = Arrays.asList(userDto1, userDto2);
        when(userService.listAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));

        verify(userService, times(1)).listAll();
    }

    @Test
    void testListAll_EmptyList() throws Exception {
        when(userService.listAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userService, times(1)).listAll();
    }

    @Test
    void testGetById_Found() throws Exception {
        when(userService.getById(userId)).thenReturn(Optional.of(userDto1));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("12345678"));

        verify(userService, times(1)).getById(userId);
    }

    @Test
    void testGetById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.getById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getById(nonExistentId);
    }
}
