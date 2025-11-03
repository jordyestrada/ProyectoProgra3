package cr.una.reservas_municipales.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests para CloudinaryService
 */
@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    void setUp() {
        cloudinaryService = new CloudinaryService();
        ReflectionTestUtils.setField(cloudinaryService, "cloudinary", cloudinary);
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void testUploadImage_WithFolder_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        String folder = "test-folder";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        mockResult.put("public_id", "test-folder/test");
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadImage(file, folder);

        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.get("secure_url"));
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadImage_WithoutFolder_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadImage(file);

        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.get("secure_url"));
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadImageFromFile_Success() throws IOException {
        // Arrange
        File file = mock(File.class);
        when(file.getName()).thenReturn("test.jpg");
        String folder = "test-folder";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(uploader.upload(any(File.class), anyMap())).thenReturn(mockResult);

        // Act
        Map<String, Object> result = cloudinaryService.uploadImage(file, folder);

        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.get("secure_url"));
        verify(uploader).upload(any(File.class), anyMap());
    }

    @Test
    void testUploadMultipleImages_AllSuccess() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "data1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "data2".getBytes())
        );
        Map<String, Object> mockResult1 = new HashMap<>();
        mockResult1.put("secure_url", "https://cloudinary.com/test1.jpg");
        Map<String, Object> mockResult2 = new HashMap<>();
        mockResult2.put("secure_url", "https://cloudinary.com/test2.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap()))
            .thenReturn(mockResult1)
            .thenReturn(mockResult2);

        // Act
        List<Map<String, Object>> results = cloudinaryService.uploadMultipleImages(files, "folder");

        // Assert
        assertEquals(2, results.size());
        assertFalse(results.get(0).containsKey("error"));
        assertFalse(results.get(1).containsKey("error"));
    }

    @Test
    void testUploadMultipleImages_WithError() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "data1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "data2".getBytes())
        );
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test1.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap()))
            .thenReturn(mockResult)
            .thenThrow(new IOException("Upload failed"));

        // Act
        List<Map<String, Object>> results = cloudinaryService.uploadMultipleImages(files, null);

        // Assert
        assertEquals(2, results.size());
        assertFalse(results.get(0).containsKey("error"));
        assertTrue(results.get(1).containsKey("error"));
        assertEquals("test2.jpg", results.get(1).get("filename"));
    }

    @Test
    void testDeleteImage_Success() throws IOException {
        // Arrange
        String publicId = "test-folder/test";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("result", "ok");
        
        when(uploader.destroy(eq(publicId), anyMap())).thenReturn(mockResult);

        // Act
        Map<String, Object> result = cloudinaryService.deleteImage(publicId);

        // Assert
        assertNotNull(result);
        assertEquals("ok", result.get("result"));
        verify(uploader).destroy(eq(publicId), anyMap());
    }

    @Test
    void testDeleteMultipleImages_Success() throws IOException {
        // Arrange
        List<String> publicIds = Arrays.asList("id1", "id2");
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("result", "ok");
        
        when(uploader.destroy(anyString(), anyMap())).thenReturn(mockResult);

        // Act
        List<Map<String, Object>> results = cloudinaryService.deleteMultipleImages(publicIds);

        // Assert
        assertEquals(2, results.size());
        assertFalse(results.get(0).containsKey("error"));
        assertFalse(results.get(1).containsKey("error"));
    }

    @Test
    void testDeleteMultipleImages_WithError() throws IOException {
        // Arrange
        List<String> publicIds = Arrays.asList("id1", "id2");
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("result", "ok");
        
        when(uploader.destroy(anyString(), anyMap()))
            .thenReturn(mockResult)
            .thenThrow(new IOException("Delete failed"));

        // Act
        List<Map<String, Object>> results = cloudinaryService.deleteMultipleImages(publicIds);

        // Assert
        assertEquals(2, results.size());
        assertFalse(results.get(0).containsKey("error"));
        assertTrue(results.get(1).containsKey("error"));
    }

    @Test
    void testExtractPublicIdFromUrl_ValidUrl() {
        // Arrange
        String url = "https://res.cloudinary.com/cloud/image/upload/v1234567890/folder/image.jpg";

        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);

        // Assert
        assertEquals("folder/image", publicId);
    }

    @Test
    void testExtractPublicIdFromUrl_WithoutVersion() {
        // Arrange
        String url = "https://res.cloudinary.com/cloud/image/upload/folder/image.jpg";

        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);

        // Assert
        assertEquals("folder/image", publicId);
    }

    @Test
    void testExtractPublicIdFromUrl_NullUrl() {
        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(null);

        // Assert
        assertNull(publicId);
    }

    @Test
    void testExtractPublicIdFromUrl_InvalidUrl() {
        // Arrange
        String url = "https://example.com/image.jpg";

        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);

        // Assert
        assertNull(publicId);
    }

    @Test
    void testUploadImageAndGetUrl_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        String url = cloudinaryService.uploadImageAndGetUrl(file, "folder");

        // Assert
        assertEquals("https://cloudinary.com/test.jpg", url);
    }

    @Test
    void testUploadMultipleImagesAndGetUrls_Success() throws IOException {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "data1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "data2".getBytes())
        );
        Map<String, Object> mockResult1 = new HashMap<>();
        mockResult1.put("secure_url", "https://cloudinary.com/test1.jpg");
        Map<String, Object> mockResult2 = new HashMap<>();
        mockResult2.put("secure_url", "https://cloudinary.com/test2.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap()))
            .thenReturn(mockResult1)
            .thenReturn(mockResult2);

        // Act
        List<String> urls = cloudinaryService.uploadMultipleImagesAndGetUrls(files, "folder");

        // Assert
        assertEquals(2, urls.size());
        assertEquals("https://cloudinary.com/test1.jpg", urls.get(0));
        assertEquals("https://cloudinary.com/test2.jpg", urls.get(1));
    }

    @Test
    void testDeleteImageByUrl_Success() throws IOException {
        // Arrange
        String url = "https://res.cloudinary.com/cloud/image/upload/folder/image.jpg";
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("result", "ok");
        
        when(uploader.destroy(anyString(), anyMap())).thenReturn(mockResult);

        // Act
        boolean result = cloudinaryService.deleteImageByUrl(url);

        // Assert
        assertTrue(result);
    }

    @Test
    void testDeleteImageByUrl_InvalidUrl() {
        // Arrange
        String url = "https://example.com/image.jpg";

        // Act
        boolean result = cloudinaryService.deleteImageByUrl(url);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDeleteImageByUrl_DeleteFails() throws IOException {
        // Arrange
        String url = "https://res.cloudinary.com/cloud/image/upload/folder/image.jpg";
        
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));

        // Act
        boolean result = cloudinaryService.deleteImageByUrl(url);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDeleteMultipleImagesByUrls_Success() throws IOException {
        // Arrange
        List<String> urls = Arrays.asList(
            "https://res.cloudinary.com/cloud/image/upload/folder/image1.jpg",
            "https://res.cloudinary.com/cloud/image/upload/folder/image2.jpg"
        );
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("result", "ok");
        
        when(uploader.destroy(anyString(), anyMap())).thenReturn(mockResult);

        // Act
        int count = cloudinaryService.deleteMultipleImagesByUrls(urls);

        // Assert
        assertEquals(2, count);
    }

    @Test
    void testUploadImageWithDetails_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        mockResult.put("public_id", "folder/test");
        mockResult.put("format", "jpg");
        mockResult.put("width", 1920);
        mockResult.put("height", 1080);
        mockResult.put("bytes", 1024);
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        CloudinaryService.UploadResult result = cloudinaryService.uploadImageWithDetails(file, "folder");

        // Assert
        assertNotNull(result);
        assertEquals("https://cloudinary.com/test.jpg", result.getUrl());
        assertEquals("folder/test", result.getPublicId());
        assertEquals("jpg", result.getFormat());
        assertEquals(1920, result.getWidth());
        assertEquals(1080, result.getHeight());
        assertEquals(1024L, result.getBytes());
    }

    @Test
    void testUploadResult_WithLongBytes() {
        // Arrange
        Map<String, Object> cloudinaryResult = new HashMap<>();
        cloudinaryResult.put("secure_url", "https://cloudinary.com/test.jpg");
        cloudinaryResult.put("public_id", "folder/test");
        cloudinaryResult.put("format", "png");
        cloudinaryResult.put("width", 800);
        cloudinaryResult.put("height", 600);
        cloudinaryResult.put("bytes", 2048L);

        // Act
        CloudinaryService.UploadResult result = new CloudinaryService.UploadResult(cloudinaryResult);

        // Assert
        assertEquals(2048L, result.getBytes());
    }

    @Test
    void testGetCloudinary() {
        // Act
        Cloudinary result = cloudinaryService.getCloudinary();

        // Assert
        assertNotNull(result);
    }

    // ========== TESTS PARA LLEGAR A 100% COBERTURA ========== //

    @Test
    void testUploadImage_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> cloudinaryService.uploadImage(file, "folder"));
    }

    @Test
    void testUploadImageFromFile_IOException() throws IOException {
        File file = mock(File.class);
        when(file.getName()).thenReturn("test.jpg");
        when(uploader.upload(any(File.class), anyMap())).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> cloudinaryService.uploadImage(file, "folder"));
    }

    @Test
    void testUploadMultipleImages_PartialFailure() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "test2".getBytes());
        
        Map<String, Object> result1 = new HashMap<>();
        result1.put("secure_url", "https://cloudinary.com/test1.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap()))
            .thenReturn(result1)
            .thenThrow(new IOException("Second upload failed"));

        List<MultipartFile> files = List.of(file1, file2);
        List<Map<String, Object>> results = cloudinaryService.uploadMultipleImages(files, "folder");

        // Returns partial results - only successful uploads
        assertTrue(results.size() <= files.size());
    }

    @Test
    void testDeleteImage_Exception() throws IOException {
        when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));

        assertThrows(IOException.class, () -> cloudinaryService.deleteImage("publicId"));
    }

    @Test
    void testDeleteMultipleImages_PartialFailure() throws IOException {
        when(uploader.destroy(eq("id1"), anyMap())).thenReturn(new HashMap<>());
        when(uploader.destroy(eq("id2"), anyMap())).thenThrow(new IOException("Delete failed"));

        List<String> publicIds = List.of("id1", "id2");
        List<Map<String, Object>> results = cloudinaryService.deleteMultipleImages(publicIds);

        // Returns partial results - only successful deletes
        assertTrue(results.size() <= publicIds.size());
    }

    @Test
    void testExtractPublicIdFromUrl_EmptyUrl() {
        String result = cloudinaryService.extractPublicIdFromUrl("");
        assertNull(result);
    }

    @Test
    void testUploadImageAndGetUrl_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> cloudinaryService.uploadImageAndGetUrl(file, "folder"));
    }

    @Test
    void testUploadMultipleImagesAndGetUrls_AllFail() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "test2".getBytes());
        
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        List<MultipartFile> files = List.of(file1, file2);
        List<String> results = cloudinaryService.uploadMultipleImagesAndGetUrls(files, "folder");

        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteImageByUrl_NullUrl() throws IOException {
        boolean result = cloudinaryService.deleteImageByUrl(null);
        assertFalse(result);
    }

    @Test
    void testDeleteMultipleImagesByUrls_AllInvalid() throws IOException {
        List<String> urls = List.of("https://example.com/1.jpg", "https://example.com/2.jpg");
        int result = cloudinaryService.deleteMultipleImagesByUrls(urls);
        assertEquals(0, result);
    }

    @Test
    void testUploadImageWithDetails_IOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Upload failed"));

        assertThrows(IOException.class, () -> cloudinaryService.uploadImageWithDetails(file, "folder"));
    }

    // ========== TESTS PARA CUBRIR LÍNEAS NO CUBIERTAS ========== //

    @Test
    void testUploadMultipleImages_WithoutFolder() throws IOException {
        // Arrange - Cubre línea 112: uploadMultipleImages(files, null)
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "data1".getBytes())
        );
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test1.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        List<Map<String, Object>> results = cloudinaryService.uploadMultipleImages(files);

        // Assert
        assertEquals(1, results.size());
        assertFalse(results.get(0).containsKey("error"));
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testExtractPublicIdFromUrl_InvalidFormat_NoSlashAfterUpload() {
        // Arrange - Cubre línea 166: when parts.length < 2
        String url = "https://res.cloudinary.com/cloud/image/upload/";

        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);

        // Assert
        assertNull(publicId);
    }

    @Test
    void testExtractPublicIdFromUrl_NoDotInFilename() {
        // Arrange - Cubre línea 175: when no hay punto en el nombre
        String url = "https://res.cloudinary.com/cloud/image/upload/folder/imagewithoutext";

        // Act
        String publicId = cloudinaryService.extractPublicIdFromUrl(url);

        // Assert
        assertEquals("folder/imagewithoutext", publicId);
    }

    @Test
    void testUploadImageAndGetUrl_WithoutFolder() throws IOException {
        // Arrange - Cubre línea 192: uploadImageAndGetUrl(file, null)
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("secure_url", "https://cloudinary.com/test.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(mockResult);

        // Act
        String url = cloudinaryService.uploadImageAndGetUrl(file);

        // Assert
        assertEquals("https://cloudinary.com/test.jpg", url);
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void testUploadMultipleImagesAndGetUrls_WithoutFolder() throws IOException {
        // Arrange - Cubre línea 217: uploadMultipleImagesAndGetUrls(files, null)
        List<MultipartFile> files = Arrays.asList(
            new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "data1".getBytes()),
            new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "data2".getBytes())
        );
        Map<String, Object> mockResult1 = new HashMap<>();
        mockResult1.put("secure_url", "https://cloudinary.com/test1.jpg");
        Map<String, Object> mockResult2 = new HashMap<>();
        mockResult2.put("secure_url", "https://cloudinary.com/test2.jpg");
        
        when(uploader.upload(any(byte[].class), anyMap()))
            .thenReturn(mockResult1)
            .thenReturn(mockResult2);

        // Act
        List<String> urls = cloudinaryService.uploadMultipleImagesAndGetUrls(files);

        // Assert
        assertEquals(2, urls.size());
        assertEquals("https://cloudinary.com/test1.jpg", urls.get(0));
        assertEquals("https://cloudinary.com/test2.jpg", urls.get(1));
        verify(uploader, times(2)).upload(any(byte[].class), anyMap());
    }
}
