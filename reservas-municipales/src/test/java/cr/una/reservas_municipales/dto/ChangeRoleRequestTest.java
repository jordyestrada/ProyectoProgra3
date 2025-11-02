package cr.una.reservas_municipales.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChangeRoleRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        assertNotNull(request);
        assertNull(request.getUserId());
        assertNull(request.getRoleCode());
    }

    @Test
    void testSettersAndGetters() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        UUID userId = UUID.randomUUID();
        
        request.setUserId(userId);
        request.setRoleCode("ROLE_ADMIN");

        assertEquals(userId, request.getUserId());
        assertEquals("ROLE_ADMIN", request.getRoleCode());
    }

    @Test
    void testValidRequest_RoleAdmin() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("ROLE_ADMIN");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidRequest_RoleSupervisor() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("ROLE_SUPERVISOR");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidRequest_RoleUser() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("ROLE_USER");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRequest_NullUserId() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(null);
        request.setRoleCode("ROLE_ADMIN");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        
        boolean hasUserIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
        assertTrue(hasUserIdError);
    }

    @Test
    void testInvalidRequest_NullRoleCode() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode(null);

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        
        boolean hasRoleCodeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("roleCode"));
        assertTrue(hasRoleCodeError);
    }

    @Test
    void testInvalidRequest_EmptyRoleCode() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidRequest_InvalidRoleCode() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("INVALID_ROLE");

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        
        boolean hasPatternError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("ROLE_ADMIN") || 
                              v.getMessage().contains("ROLE_SUPERVISOR") || 
                              v.getMessage().contains("ROLE_USER"));
        assertTrue(hasPatternError);
    }

    @Test
    void testInvalidRequest_IncorrectCasing() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("role_admin"); // lowercase

        Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID userId = UUID.randomUUID();
        
        ChangeRoleRequest request1 = new ChangeRoleRequest();
        request1.setUserId(userId);
        request1.setRoleCode("ROLE_ADMIN");
        
        ChangeRoleRequest request2 = new ChangeRoleRequest();
        request2.setUserId(userId);
        request2.setRoleCode("ROLE_ADMIN");
        
        ChangeRoleRequest request3 = new ChangeRoleRequest();
        request3.setUserId(UUID.randomUUID());
        request3.setRoleCode("ROLE_USER");

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        ChangeRoleRequest request = new ChangeRoleRequest();
        request.setUserId(UUID.randomUUID());
        request.setRoleCode("ROLE_SUPERVISOR");
        
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ChangeRoleRequest"));
        assertTrue(toString.contains("ROLE_SUPERVISOR"));
    }

    @Test
    void testAllValidRoles() {
        String[] validRoles = {"ROLE_ADMIN", "ROLE_SUPERVISOR", "ROLE_USER"};
        
        for (String role : validRoles) {
            ChangeRoleRequest request = new ChangeRoleRequest();
            request.setUserId(UUID.randomUUID());
            request.setRoleCode(role);
            
            Set<ConstraintViolation<ChangeRoleRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Role " + role + " should be valid");
        }
    }
}
