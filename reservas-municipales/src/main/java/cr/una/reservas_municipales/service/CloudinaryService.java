package cr.una.reservas_municipales.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar la subida y eliminación de imágenes en Cloudinary.
 */
@Slf4j
@Service
public class CloudinaryService {

    private static final String CLOUD_NAME = "dppt5sr0b";
    private static final String API_KEY = "338975877244789";
    private static final String API_SECRET = "L4N6IDO4OfFeUO5_2miKS0Koh9g";

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("api_key", API_KEY);
        config.put("api_secret", API_SECRET);
        this.cloudinary = new Cloudinary(config);
        log.info("CloudinaryService inicializado correctamente");
    }

    /**
     * Sube una imagen a Cloudinary desde un MultipartFile
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        log.info("Iniciando subida de imagen: {}", file.getOriginalFilename());
        
        Map<String, Object> uploadParams = new HashMap<>();
        
        if (folder != null && !folder.isEmpty()) {
            uploadParams.put("folder", folder);
        }
        
        uploadParams.put("resource_type", "image");
        uploadParams.put("use_filename", true);
        uploadParams.put("unique_filename", true);
        
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        log.info("Imagen subida exitosamente. URL: {}", result.get("secure_url"));
        return result;
    }

    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, null);
    }

    public Map<String, Object> uploadImage(File file, String folder) throws IOException {
        log.info("Iniciando subida de imagen desde File: {}", file.getName());
        
        Map<String, Object> uploadParams = new HashMap<>();
        
        if (folder != null && !folder.isEmpty()) {
            uploadParams.put("folder", folder);
        }
        
        uploadParams.put("resource_type", "image");
        uploadParams.put("use_filename", true);
        uploadParams.put("unique_filename", true);
        
        Map<String, Object> result = cloudinary.uploader().upload(file, uploadParams);
        
        log.info("Imagen subida exitosamente. URL: {}", result.get("secure_url"));
        return result;
    }

    /**
     * Sube múltiples imágenes a Cloudinary
     */
    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder) {
        log.info("Iniciando subida de {} imágenes", files.size());
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                Map<String, Object> result = uploadImage(file, folder);
                results.add(result);
            } catch (IOException e) {
                log.error("Error al subir imagen {}: {}", file.getOriginalFilename(), e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("filename", file.getOriginalFilename());
                results.add(errorResult);
            }
        }
        
        log.info("Subida completada. {} de {} imágenes subidas exitosamente", 
                 results.stream().filter(r -> !r.containsKey("error")).count(), 
                 files.size());
        
        return results;
    }

    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files) {
        return uploadMultipleImages(files, null);
    }

    /**
     * Elimina una imagen de Cloudinary usando su public_id
     */
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        log.info("Eliminando imagen con public_id: {}", publicId);
        
        Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        
        log.info("Imagen eliminada. Resultado: {}", result.get("result"));
        return result;
    }

    /**
     * Elimina múltiples imágenes de Cloudinary
     */
    public List<Map<String, Object>> deleteMultipleImages(List<String> publicIds) {
        log.info("Eliminando {} imágenes", publicIds.size());
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (String publicId : publicIds) {
            try {
                Map<String, Object> result = deleteImage(publicId);
                results.add(result);
            } catch (IOException e) {
                log.error("Error al eliminar imagen {}: {}", publicId, e.getMessage());
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", e.getMessage());
                errorResult.put("public_id", publicId);
                results.add(errorResult);
            }
        }
        
        log.info("Eliminación completada. {} de {} imágenes eliminadas exitosamente", 
                 results.stream().filter(r -> !r.containsKey("error")).count(), 
                 publicIds.size());
        
        return results;
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     */
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        if (cloudinaryUrl == null || !cloudinaryUrl.contains("/upload/")) {
            log.warn("URL no válida para extraer public_id: {}", cloudinaryUrl);
            return null;
        }
        
        String[] parts = cloudinaryUrl.split("/upload/");
        if (parts.length < 2) {
            return null;
        }
        
        String afterUpload = parts[1];
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }
        
        int lastDotIndex = afterUpload.lastIndexOf('.');
        if (lastDotIndex > 0) {
            afterUpload = afterUpload.substring(0, lastDotIndex);
        }
        
        log.info("Public ID extraído: {}", afterUpload);
        return afterUpload;
    }

    /**
     * Sube una imagen y devuelve solo la URL segura
     */
    public String uploadImageAndGetUrl(MultipartFile file, String folder) throws IOException {
        Map<String, Object> result = uploadImage(file, folder);
        return (String) result.get("secure_url");
    }

    public String uploadImageAndGetUrl(MultipartFile file) throws IOException {
        return uploadImageAndGetUrl(file, null);
    }

    /**
     * Sube múltiples imágenes y devuelve solo las URLs
     */
    public List<String> uploadMultipleImagesAndGetUrls(List<MultipartFile> files, String folder) {
        log.info("Iniciando subida de {} imágenes y extrayendo URLs", files.size());
        
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String url = uploadImageAndGetUrl(file, folder);
                urls.add(url);
            } catch (IOException e) {
                log.error("Error al subir imagen {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }
        
        log.info("Subida completada. {} de {} imágenes subidas exitosamente", urls.size(), files.size());
        return urls;
    }

    public List<String> uploadMultipleImagesAndGetUrls(List<MultipartFile> files) {
        return uploadMultipleImagesAndGetUrls(files, null);
    }

    /**
     * Elimina una imagen usando su URL completa de Cloudinary
     */
    public boolean deleteImageByUrl(String cloudinaryUrl) {
        try {
            String publicId = extractPublicIdFromUrl(cloudinaryUrl);
            if (publicId == null) {
                log.error("No se pudo extraer public_id de la URL: {}", cloudinaryUrl);
                return false;
            }
            
            Map<String, Object> result = deleteImage(publicId);
            String resultStatus = (String) result.get("result");
            return "ok".equalsIgnoreCase(resultStatus);
        } catch (IOException e) {
            log.error("Error al eliminar imagen por URL {}: {}", cloudinaryUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Elimina múltiples imágenes usando sus URLs
     */
    public int deleteMultipleImagesByUrls(List<String> cloudinaryUrls) {
        log.info("Eliminando {} imágenes por URL", cloudinaryUrls.size());
        
        int deletedCount = 0;
        
        for (String url : cloudinaryUrls) {
            if (deleteImageByUrl(url)) {
                deletedCount++;
            }
        }
        
        log.info("Eliminación completada. {} de {} imágenes eliminadas exitosamente", 
                 deletedCount, cloudinaryUrls.size());
        
        return deletedCount;
    }

    /**
     * Sube una imagen y devuelve información completa
     */
    public UploadResult uploadImageWithDetails(MultipartFile file, String folder) throws IOException {
        Map<String, Object> result = uploadImage(file, folder);
        return new UploadResult(result);
    }

    /**
     * Clase interna para información completa de la imagen subida
     */
    public static class UploadResult {
        private final String url;
        private final String publicId;
        private final String format;
        private final Integer width;
        private final Integer height;
        private final Long bytes;

        public UploadResult(Map<String, Object> cloudinaryResult) {
            this.url = (String) cloudinaryResult.get("secure_url");
            this.publicId = (String) cloudinaryResult.get("public_id");
            this.format = (String) cloudinaryResult.get("format");
            this.width = (Integer) cloudinaryResult.get("width");
            this.height = (Integer) cloudinaryResult.get("height");
            this.bytes = cloudinaryResult.get("bytes") instanceof Integer ? 
                         ((Integer) cloudinaryResult.get("bytes")).longValue() : 
                         (Long) cloudinaryResult.get("bytes");
        }

        public String getUrl() { return url; }
        public String getPublicId() { return publicId; }
        public String getFormat() { return format; }
        public Integer getWidth() { return width; }
        public Integer getHeight() { return height; }
        public Long getBytes() { return bytes; }
    }

    public Cloudinary getCloudinary() {
        return cloudinary;
    }
}
