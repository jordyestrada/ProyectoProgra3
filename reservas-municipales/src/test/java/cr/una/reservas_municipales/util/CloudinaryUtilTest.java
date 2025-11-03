package cr.una.reservas_municipales.util;

import com.cloudinary.Cloudinary;
import cr.una.reservas_municipales.service.CloudinaryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para CloudinaryUtil
 * Nota: Estos tests usan reflection para mockear el CloudinaryService interno
 */
@ExtendWith(MockitoExtension.class)
class CloudinaryUtilTest {

    @Mock
    private CloudinaryService mockCloudinaryService;

    private CloudinaryService originalService;

    @BeforeEach
    void setUp() throws Exception {
        // Guardar el servicio original
        Field serviceField = CloudinaryUtil.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        originalService = (CloudinaryService) serviceField.get(null);
        
        // Inyectar el mock
        serviceField.set(null, mockCloudinaryService);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restaurar el servicio original
        Field serviceField = CloudinaryUtil.class.getDeclaredField("service");
        serviceField.setAccessible(true);
        serviceField.set(null, originalService);
    }

    // ========== Tests para métodos de upload ==========

    @Test
    void testUploadImage_MultipartFile_WithFolder() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        String folder = "test-folder";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("secure_url", "https://cloudinary.com/test.jpg");
        expectedResult.put("public_id", "test-folder/test");
        
        when(mockCloudinaryService.uploadImage(eq(file), eq(folder))).thenReturn(expectedResult);
        
        // Act
        Map<String, Object> result = CloudinaryUtil.uploadImage(file, folder);
        
        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.get("secure_url"));
        verify(mockCloudinaryService).uploadImage(file, folder);
    }

    @Test
    void testUploadImage_MultipartFile_DefaultFolder() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(mockCloudinaryService.uploadImage(eq(file))).thenReturn(expectedResult);
        
        // Act
        Map<String, Object> result = CloudinaryUtil.uploadImage(file);
        
        // Assert
        assertNotNull(result);
        verify(mockCloudinaryService).uploadImage(file);
    }

    @Test
    void testUploadImage_File_WithFolder() throws IOException {
        // Arrange
        File file = new File("test.jpg");
        String folder = "test-folder";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(mockCloudinaryService.uploadImage(eq(file), eq(folder))).thenReturn(expectedResult);
        
        // Act
        Map<String, Object> result = CloudinaryUtil.uploadImage(file, folder);
        
        // Assert
        assertNotNull(result);
        verify(mockCloudinaryService).uploadImage(file, folder);
    }

    @Test
    void testUploadMultipleImages_WithFolder() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "test2".getBytes())
        );
        String folder = "test-folder";
        List<Map<String, Object>> expectedResults = Arrays.asList(
            Map.of("secure_url", "https://cloudinary.com/test1.jpg"),
            Map.of("secure_url", "https://cloudinary.com/test2.jpg")
        );
        
        when(mockCloudinaryService.uploadMultipleImages(eq(files), eq(folder))).thenReturn(expectedResults);
        
        // Act
        List<Map<String, Object>> results = CloudinaryUtil.uploadMultipleImages(files, folder);
        
        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(mockCloudinaryService).uploadMultipleImages(files, folder);
    }

    @Test
    void testUploadMultipleImages_DefaultFolder() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes())
        );
        List<Map<String, Object>> expectedResults = Arrays.asList(
            Map.of("secure_url", "https://cloudinary.com/test1.jpg")
        );
        
        when(mockCloudinaryService.uploadMultipleImages(eq(files))).thenReturn(expectedResults);
        
        // Act
        List<Map<String, Object>> results = CloudinaryUtil.uploadMultipleImages(files);
        
        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(mockCloudinaryService).uploadMultipleImages(files);
    }

    @Test
    void testUploadImageAndGetUrl_WithFolder() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        String folder = "test-folder";
        String expectedUrl = "https://cloudinary.com/test.jpg";
        
        when(mockCloudinaryService.uploadImageAndGetUrl(eq(file), eq(folder))).thenReturn(expectedUrl);
        
        // Act
        String url = CloudinaryUtil.uploadImageAndGetUrl(file, folder);
        
        // Assert
        assertEquals(expectedUrl, url);
        verify(mockCloudinaryService).uploadImageAndGetUrl(file, folder);
    }

    @Test
    void testUploadImageAndGetUrl_DefaultFolder() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        String expectedUrl = "https://cloudinary.com/test.jpg";
        
        when(mockCloudinaryService.uploadImageAndGetUrl(eq(file))).thenReturn(expectedUrl);
        
        // Act
        String url = CloudinaryUtil.uploadImageAndGetUrl(file);
        
        // Assert
        assertEquals(expectedUrl, url);
        verify(mockCloudinaryService).uploadImageAndGetUrl(file);
    }

    @Test
    void testUploadMultipleImagesAndGetUrls_WithFolder() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "test2".getBytes())
        );
        String folder = "test-folder";
        List<String> expectedUrls = Arrays.asList(
            "https://cloudinary.com/test1.jpg",
            "https://cloudinary.com/test2.jpg"
        );
        
        when(mockCloudinaryService.uploadMultipleImagesAndGetUrls(eq(files), eq(folder))).thenReturn(expectedUrls);
        
        // Act
        List<String> urls = CloudinaryUtil.uploadMultipleImagesAndGetUrls(files, folder);
        
        // Assert
        assertNotNull(urls);
        assertEquals(2, urls.size());
        verify(mockCloudinaryService).uploadMultipleImagesAndGetUrls(files, folder);
    }

    @Test
    void testUploadMultipleImagesAndGetUrls_DefaultFolder() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes())
        );
        List<String> expectedUrls = Arrays.asList("https://cloudinary.com/test1.jpg");
        
        when(mockCloudinaryService.uploadMultipleImagesAndGetUrls(eq(files))).thenReturn(expectedUrls);
        
        // Act
        List<String> urls = CloudinaryUtil.uploadMultipleImagesAndGetUrls(files);
        
        // Assert
        assertNotNull(urls);
        assertEquals(1, urls.size());
        verify(mockCloudinaryService).uploadMultipleImagesAndGetUrls(files);
    }

    @Test
    void testUploadImageWithDetails() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        String folder = "test-folder";
        
        // Crear el mapa para el resultado
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("secure_url", "https://cloudinary.com/test.jpg");
        resultMap.put("public_id", "test-folder/test");
        resultMap.put("format", "jpg");
        resultMap.put("width", 1920);
        resultMap.put("height", 1080);
        resultMap.put("bytes", 1024);
        
        CloudinaryService.UploadResult serviceResult = new CloudinaryService.UploadResult(resultMap);
        
        when(mockCloudinaryService.uploadImageWithDetails(eq(file), eq(folder))).thenReturn(serviceResult);
        
        // Act
        CloudinaryUtil.UploadResult result = CloudinaryUtil.uploadImageWithDetails(file, folder);
        
        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.getUrl());
        assertEquals("test-folder/test", result.getPublicId());
        assertEquals("jpg", result.getFormat());
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
        assertEquals(1024L, result.getBytes());
        verify(mockCloudinaryService).uploadImageWithDetails(file, folder);
    }

    @Test
    void testExtractPublicIdFromUrl() {
        // Arrange
        String url = "https://res.cloudinary.com/dppt5sr0b/image/upload/v1234567890/folder/image.jpg";
        String expectedPublicId = "folder/image";
        
        when(mockCloudinaryService.extractPublicIdFromUrl(eq(url))).thenReturn(expectedPublicId);
        
        // Act
        String publicId = CloudinaryUtil.extractPublicIdFromUrl(url);
        
        // Assert
        assertEquals(expectedPublicId, publicId);
        verify(mockCloudinaryService).extractPublicIdFromUrl(url);
    }

    @Test
    void testGetCloudinary() {
        // Arrange
        Cloudinary expectedCloudinary = new Cloudinary();
        when(mockCloudinaryService.getCloudinary()).thenReturn(expectedCloudinary);
        
        // Act
        Cloudinary cloudinary = CloudinaryUtil.getCloudinary();
        
        // Assert
        assertEquals(expectedCloudinary, cloudinary);
        verify(mockCloudinaryService).getCloudinary();
    }

    // ========== Tests para clases internas ==========

    @Test
    void testUploadResult_Constructor() {
        // Arrange
        Map<String, Object> cloudinaryResult = new HashMap<>();
        cloudinaryResult.put("secure_url", "https://example.com/image.jpg");
        cloudinaryResult.put("public_id", "folder/image");
        cloudinaryResult.put("format", "jpg");
        cloudinaryResult.put("width", 1920);
        cloudinaryResult.put("height", 1080);
        cloudinaryResult.put("bytes", 1024);
        
        // Act
        CloudinaryUtil.UploadResult result = new CloudinaryUtil.UploadResult(cloudinaryResult);
        
        // Assert
        assertEquals("https://example.com/image.jpg", result.getUrl());
        assertEquals("folder/image", result.getPublicId());
        assertEquals("jpg", result.getFormat());
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
        assertEquals(1024L, result.getBytes());
    }

    @Test
    void testUploadResult_ConstructorWithLongBytes() {
        // Arrange
        Map<String, Object> cloudinaryResult = new HashMap<>();
        cloudinaryResult.put("secure_url", "https://example.com/image.jpg");
        cloudinaryResult.put("public_id", "folder/image");
        cloudinaryResult.put("format", "png");
        cloudinaryResult.put("width", 800);
        cloudinaryResult.put("height", 600);
        cloudinaryResult.put("bytes", 2048L); // Long instead of Integer
        
        // Act
        CloudinaryUtil.UploadResult result = new CloudinaryUtil.UploadResult(cloudinaryResult);
        
        // Assert
        assertEquals("https://example.com/image.jpg", result.getUrl());
        assertEquals("folder/image", result.getPublicId());
        assertEquals("png", result.getFormat());
        assertEquals(800, result.getWidth());
        assertEquals(600, result.getHeight());
        assertEquals(2048L, result.getBytes());
    }

    // ========== TEST PARA CONSTRUCTOR POR DEFECTO ==========

    @Test
    void testConstructor_CanBeInstantiated() throws Exception {
        // Arrange - Acceder al constructor por defecto usando reflection
        java.lang.reflect.Constructor<CloudinaryUtil> constructor = CloudinaryUtil.class.getDeclaredConstructor();
        
        // Act - Crear instancia usando el constructor
        constructor.setAccessible(true);
        CloudinaryUtil instance = constructor.newInstance();
        
        // Assert - Verificar que se puede crear la instancia
        assertNotNull(instance, "Should be able to create instance via reflection");
        
        // Verificar que es una instancia de CloudinaryUtil
        assertTrue(instance instanceof CloudinaryUtil, "Instance should be of type CloudinaryUtil");
    }

}
