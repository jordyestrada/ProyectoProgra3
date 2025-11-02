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

class SpaceImageDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        SpaceImageDto dto = new SpaceImageDto();
        assertNotNull(dto);
        assertNull(dto.getImageId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getUrl());
    }

    @Test
    void testSettersAndGetters() {
        SpaceImageDto dto = new SpaceImageDto();
        Long imageId = 123L;
        UUID spaceId = UUID.randomUUID();
        String url = "https://example.com/images/space1.jpg";

        dto.setImageId(imageId);
        dto.setSpaceId(spaceId);
        dto.setUrl(url);
        dto.setMain(true);
        dto.setDisplayOrder(1);

        assertEquals(imageId, dto.getImageId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(url, dto.getUrl());
        assertTrue(dto.getMain());
        assertEquals(1, dto.getDisplayOrder());
    }

    @Test
    void testValidSpaceImage() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/image.jpg");

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidSpaceImage_NullSpaceId() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(null);
        dto.setUrl("https://example.com/image.jpg");

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasSpaceIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("spaceId"));
        assertTrue(hasSpaceIdError);
    }

    @Test
    void testInvalidSpaceImage_NullUrl() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl(null);

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasUrlError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("url"));
        assertTrue(hasUrlError);
    }

    @Test
    void testInvalidSpaceImage_EmptyUrl() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("");

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidSpaceImage_BlankUrl() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("   ");

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testMainImage() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/main.jpg");
        dto.setMain(true);

        assertTrue(dto.getMain());
        
        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testSecondaryImage() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/secondary.jpg");
        dto.setMain(false);

        assertFalse(dto.getMain());
    }

    @Test
    void testDisplayOrder() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/image.jpg");
        dto.setDisplayOrder(5);

        assertEquals(5, dto.getDisplayOrder());
    }

    @Test
    void testDisplayOrderVariousValues() {
        Integer[] orders = {0, 1, 10, 100, -1};
        
        for (Integer order : orders) {
            SpaceImageDto dto = new SpaceImageDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUrl("https://example.com/image.jpg");
            dto.setDisplayOrder(order);
            
            assertEquals(order, dto.getDisplayOrder());
        }
    }

    @Test
    void testDifferentUrlFormats() {
        String[] urls = {
            "https://example.com/image.jpg",
            "http://example.com/image.png",
            "https://cdn.example.com/spaces/12345/photo.webp",
            "/static/images/space1.jpg",
            "data:image/png;base64,iVBORw0KGgo..."
        };
        
        for (String url : urls) {
            SpaceImageDto dto = new SpaceImageDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUrl(url);
            
            Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "URL " + url + " should be valid");
        }
    }

    @Test
    void testImageWithAllFields() {
        SpaceImageDto dto = new SpaceImageDto();
        Long imageId = 456L;
        UUID spaceId = UUID.randomUUID();
        
        dto.setImageId(imageId);
        dto.setSpaceId(spaceId);
        dto.setUrl("https://storage.example.com/images/space123.jpg");
        dto.setMain(true);
        dto.setDisplayOrder(0);

        assertEquals(imageId, dto.getImageId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals("https://storage.example.com/images/space123.jpg", dto.getUrl());
        assertTrue(dto.getMain());
        assertEquals(0, dto.getDisplayOrder());
    }

    @Test
    void testImageWithOptionalFields() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/image.jpg");

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        
        assertNull(dto.getImageId());
        assertNull(dto.getMain());
        assertNull(dto.getDisplayOrder());
    }

    @Test
    void testEqualsAndHashCode() {
        Long imageId = 123L;
        UUID spaceId = UUID.randomUUID();
        
        SpaceImageDto dto1 = new SpaceImageDto();
        dto1.setImageId(imageId);
        dto1.setSpaceId(spaceId);
        dto1.setUrl("https://example.com/image.jpg");
        
        SpaceImageDto dto2 = new SpaceImageDto();
        dto2.setImageId(imageId);
        dto2.setSpaceId(spaceId);
        dto2.setUrl("https://example.com/image.jpg");
        
        SpaceImageDto dto3 = new SpaceImageDto();
        dto3.setImageId(456L);
        dto3.setSpaceId(UUID.randomUUID());
        dto3.setUrl("https://example.com/other.jpg");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setImageId(789L);
        dto.setSpaceId(UUID.randomUUID());
        dto.setUrl("https://example.com/image.jpg");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SpaceImageDto"));
    }

    @Test
    void testMultipleImagesForSameSpace() {
        UUID spaceId = UUID.randomUUID();
        
        SpaceImageDto image1 = new SpaceImageDto();
        image1.setSpaceId(spaceId);
        image1.setUrl("https://example.com/image1.jpg");
        image1.setMain(true);
        image1.setDisplayOrder(0);
        
        SpaceImageDto image2 = new SpaceImageDto();
        image2.setSpaceId(spaceId);
        image2.setUrl("https://example.com/image2.jpg");
        image2.setMain(false);
        image2.setDisplayOrder(1);
        
        SpaceImageDto image3 = new SpaceImageDto();
        image3.setSpaceId(spaceId);
        image3.setUrl("https://example.com/image3.jpg");
        image3.setMain(false);
        image3.setDisplayOrder(2);

        assertEquals(spaceId, image1.getSpaceId());
        assertEquals(spaceId, image2.getSpaceId());
        assertEquals(spaceId, image3.getSpaceId());
        
        assertTrue(image1.getMain());
        assertFalse(image2.getMain());
        assertFalse(image3.getMain());
    }

    @Test
    void testLongUrl() {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID());
        String longUrl = "https://example.com/" + "a".repeat(500) + ".jpg";
        dto.setUrl(longUrl);

        Set<ConstraintViolation<SpaceImageDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals(longUrl, dto.getUrl());
    }
}
