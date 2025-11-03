package cr.una.reservas_municipales.util;

import com.cloudinary.Cloudinary;
import cr.una.reservas_municipales.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utilidad estática para gestionar imágenes en Cloudinary.
 * DEPRECATED: Usar CloudinaryService inyectado en su lugar para mejor testabilidad.
 * Esta clase se mantiene solo para backward compatibility.
 */
@Deprecated
public class CloudinaryUtil {

    private static CloudinaryService service = new CloudinaryService();

    public static Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        return service.uploadImage(file, folder);
    }

    public static Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        return service.uploadImage(file);
    }

    public static Map<String, Object> uploadImage(File file, String folder) throws IOException {
        return service.uploadImage(file, folder);
    }

    public static List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder) {
        return service.uploadMultipleImages(files, folder);
    }

    public static List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files) {
        return service.uploadMultipleImages(files);
    }

    public static String extractPublicIdFromUrl(String cloudinaryUrl) {
        return service.extractPublicIdFromUrl(cloudinaryUrl);
    }

    public static String uploadImageAndGetUrl(MultipartFile file, String folder) throws IOException {
        return service.uploadImageAndGetUrl(file, folder);
    }

    public static String uploadImageAndGetUrl(MultipartFile file) throws IOException {
        return service.uploadImageAndGetUrl(file);
    }

    public static List<String> uploadMultipleImagesAndGetUrls(List<MultipartFile> files, String folder) {
        return service.uploadMultipleImagesAndGetUrls(files, folder);
    }

    public static List<String> uploadMultipleImagesAndGetUrls(List<MultipartFile> files) {
        return service.uploadMultipleImagesAndGetUrls(files);
    }

    /**
     * @deprecated Usar CloudinaryService.UploadResult
     */
    @Deprecated
    public static class UploadResult {
        private final CloudinaryService.UploadResult delegate;

        public UploadResult(Map<String, Object> cloudinaryResult) {
            this.delegate = new CloudinaryService.UploadResult(cloudinaryResult);
        }

        public String getUrl() { return delegate.getUrl(); }
        public String getPublicId() { return delegate.getPublicId(); }
        public String getFormat() { return delegate.getFormat(); }
        public Integer getWidth() { return delegate.getWidth(); }
        public Integer getHeight() { return delegate.getHeight(); }
        public Long getBytes() { return delegate.getBytes(); }
    }

    public static UploadResult uploadImageWithDetails(MultipartFile file, String folder) throws IOException {
        CloudinaryService.UploadResult result = service.uploadImageWithDetails(file, folder);
        Map<String, Object> resultMap = new java.util.HashMap<>();
        resultMap.put("secure_url", result.getUrl());
        resultMap.put("public_id", result.getPublicId());
        resultMap.put("format", result.getFormat());
        resultMap.put("width", result.getWidth());
        resultMap.put("height", result.getHeight());
        resultMap.put("bytes", result.getBytes());
        return new UploadResult(resultMap);
    }

    public static Cloudinary getCloudinary() {
        return service.getCloudinary();
    }
}
