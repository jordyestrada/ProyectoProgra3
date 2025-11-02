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

class SpaceDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        SpaceDto dto = new SpaceDto();
        assertNotNull(dto);
        assertNull(dto.getSpaceId());
        assertNull(dto.getName());
    }

    @Test
    void testSettersAndGetters() {
        SpaceDto dto = new SpaceDto();
        UUID spaceId = UUID.randomUUID();

        dto.setSpaceId(spaceId);
        dto.setName("Salón Principal");
        dto.setCapacity(100);
        dto.setLocation("Edificio Central");
        dto.setOutdoor(false);
        dto.setActive(true);
        dto.setDescription("Salón amplio para eventos");

        assertEquals(spaceId, dto.getSpaceId());
        assertEquals("Salón Principal", dto.getName());
        assertEquals(100, dto.getCapacity());
        assertEquals("Edificio Central", dto.getLocation());
        assertFalse(dto.isOutdoor());
        assertTrue(dto.isActive());
        assertEquals("Salón amplio para eventos", dto.getDescription());
    }

    @Test
    void testValidSpace() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Cancha de Fútbol");
        dto.setCapacity(50);
        dto.setLocation("Complejo Deportivo");
        dto.setOutdoor(true);
        dto.setActive(true);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_NullName() {
        SpaceDto dto = new SpaceDto();
        dto.setName(null);
        dto.setCapacity(50);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertTrue(hasNameError);
    }

    @Test
    void testInvalidSpace_EmptyName() {
        SpaceDto dto = new SpaceDto();
        dto.setName("");
        dto.setCapacity(50);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_BlankName() {
        SpaceDto dto = new SpaceDto();
        dto.setName("   ");
        dto.setCapacity(50);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_NameTooLong() {
        SpaceDto dto = new SpaceDto();
        dto.setName("A".repeat(256)); // Exceeds 255 characters
        dto.setCapacity(50);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_NullCapacity() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(null);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasCapacityError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("capacity"));
        assertTrue(hasCapacityError);
    }

    @Test
    void testInvalidSpace_ZeroCapacity() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(0);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_NegativeCapacity() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(-10);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidSpace_MinimalCapacity() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Sala Pequeña");
        dto.setCapacity(1);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidSpace_LargeCapacity() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Estadio");
        dto.setCapacity(50000);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_LocationTooLong() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(50);
        dto.setLocation("A".repeat(501)); // Exceeds 500 characters

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpace_DescriptionTooLong() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(50);
        dto.setDescription("A".repeat(1001)); // Exceeds 1000 characters

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testOutdoorSpace() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Cancha Exterior");
        dto.setCapacity(100);
        dto.setOutdoor(true);

        assertTrue(dto.isOutdoor());
        
        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testIndoorSpace() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón Interior");
        dto.setCapacity(50);
        dto.setOutdoor(false);

        assertFalse(dto.isOutdoor());
    }

    @Test
    void testActiveSpace() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón Activo");
        dto.setCapacity(50);
        dto.setActive(true);

        assertTrue(dto.isActive());
    }

    @Test
    void testInactiveSpace() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón Inactivo");
        dto.setCapacity(50);
        dto.setActive(false);

        assertFalse(dto.isActive());
    }

    @Test
    void testSpaceWithAllFields() {
        SpaceDto dto = new SpaceDto();
        UUID spaceId = UUID.randomUUID();
        
        dto.setSpaceId(spaceId);
        dto.setName("Salón Multiusos");
        dto.setCapacity(150);
        dto.setLocation("Piso 3, Edificio A");
        dto.setOutdoor(false);
        dto.setActive(true);
        dto.setDescription("Salón equipado con proyector, aire acondicionado y sistema de sonido");

        assertEquals(spaceId, dto.getSpaceId());
        assertEquals("Salón Multiusos", dto.getName());
        assertEquals(150, dto.getCapacity());
        assertEquals("Piso 3, Edificio A", dto.getLocation());
        assertFalse(dto.isOutdoor());
        assertTrue(dto.isActive());
        assertEquals("Salón equipado con proyector, aire acondicionado y sistema de sonido", dto.getDescription());
    }

    @Test
    void testSpaceWithOptionalFields() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Espacio Básico");
        dto.setCapacity(25);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        
        assertNull(dto.getSpaceId());
        assertNull(dto.getLocation());
        assertNull(dto.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        
        SpaceDto dto1 = new SpaceDto();
        dto1.setSpaceId(id);
        dto1.setName("Salón A");
        
        SpaceDto dto2 = new SpaceDto();
        dto2.setSpaceId(id);
        dto2.setName("Salón A");
        
        SpaceDto dto3 = new SpaceDto();
        dto3.setSpaceId(UUID.randomUUID());
        dto3.setName("Salón B");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        SpaceDto dto = new SpaceDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setName("Salón de Conferencias");
        dto.setCapacity(75);
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SpaceDto"));
        assertTrue(toString.contains("Salón de Conferencias"));
    }

    @Test
    void testMaxLengthName() {
        SpaceDto dto = new SpaceDto();
        dto.setName("A".repeat(255)); // Exactly 255 characters
        dto.setCapacity(50);

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMaxLengthLocation() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(50);
        dto.setLocation("A".repeat(500)); // Exactly 500 characters

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testMaxLengthDescription() {
        SpaceDto dto = new SpaceDto();
        dto.setName("Salón");
        dto.setCapacity(50);
        dto.setDescription("A".repeat(1000)); // Exactly 1000 characters

        Set<ConstraintViolation<SpaceDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}
