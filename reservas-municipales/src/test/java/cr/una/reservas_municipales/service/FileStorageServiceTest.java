package cr.una.reservas_municipales.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para FileStorageService
 */
class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
    }

    @Test
    void testServiceAnnotationPresent() {
        assertTrue(FileStorageService.class.isAnnotationPresent(Service.class));
    }

    @Test
    void testFileStorageServiceCanBeInstantiated() {
        assertNotNull(fileStorageService);
    }

    @Test
    void testFileStorageServiceIsInCorrectPackage() {
        assertEquals("cr.una.reservas_municipales.service", 
                     FileStorageService.class.getPackageName());
    }

    @Test
    void testClassExists() {
        assertDoesNotThrow(() -> Class.forName("cr.una.reservas_municipales.service.FileStorageService"));
    }

    @Test
    void testServiceHasPublicConstructor() {
        assertDoesNotThrow(() -> FileStorageService.class.getDeclaredConstructor());
    }
}
