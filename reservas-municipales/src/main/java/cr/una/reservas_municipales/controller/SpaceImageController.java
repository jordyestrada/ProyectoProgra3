package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.SpaceImageDto;
import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.service.SpaceImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/spaces/{spaceId}/images")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SpaceImageController {

    private final SpaceImageService imageService;

    /**
     * GET /api/spaces/{spaceId}/images - Obtener todas las imágenes de un espacio
     */
    @GetMapping
    public ResponseEntity<List<SpaceImage>> getSpaceImages(@PathVariable UUID spaceId) {
        log.info("GET /api/spaces/{}/images - Getting images", spaceId);
        try {
            List<SpaceImage> images = imageService.getImagesBySpace(spaceId);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            log.error("Error getting images for space: " + spaceId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/spaces/{spaceId}/images - Agregar nueva imagen
     * Solo ADMIN y SUPERVISOR pueden agregar imágenes
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> addImage(@PathVariable UUID spaceId,
                                          @Valid @RequestBody SpaceImageDto dto) {
        log.info("POST /api/spaces/{}/images - Adding new image", spaceId);
        
        try {
            // Validar que el spaceId del path coincida con el del body
            if (!spaceId.equals(dto.getSpaceId())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Space ID mismatch");
                error.put("message", "Space ID in path must match Space ID in body");
                return ResponseEntity.badRequest().body(error);
            }

            SpaceImage savedImage = imageService.addImage(
                dto.getSpaceId(), 
                dto.getUrl(), 
                dto.getMain() != null ? dto.getMain() : false
            );
            
            log.info("Image added successfully: {}", savedImage.getImageId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error adding image to space: " + spaceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add image");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * PUT /api/spaces/{spaceId}/images/{imageId} - Actualizar imagen
     * Solo ADMIN y SUPERVISOR pueden actualizar imágenes
     */
    @PutMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> updateImage(@PathVariable UUID spaceId,
                                             @PathVariable Long imageId,
                                             @Valid @RequestBody SpaceImageDto dto) {
        log.info("PUT /api/spaces/{}/images/{} - Updating image", spaceId, imageId);
        
        try {
            var updatedImage = imageService.updateImage(imageId, dto.getUrl(), dto.getMain());
            
            if (updatedImage.isPresent()) {
                log.info("Image updated successfully: {}", imageId);
                return ResponseEntity.ok(updatedImage.get());
            } else {
                log.warn("Image not found: {}", imageId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error updating image: " + imageId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update image");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * DELETE /api/spaces/{spaceId}/images/{imageId} - Eliminar imagen
     * Solo ADMIN puede eliminar imágenes
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteImage(@PathVariable UUID spaceId,
                                             @PathVariable Long imageId) {
        log.info("DELETE /api/spaces/{}/images/{} - Deleting image", spaceId, imageId);
        
        try {
            boolean deleted = imageService.deleteImage(imageId);
            
            if (deleted) {
                log.info("Image deleted successfully: {}", imageId);
                Map<String, String> success = new HashMap<>();
                success.put("message", "Image deleted successfully");
                return ResponseEntity.ok(success);
            } else {
                log.warn("Image not found: {}", imageId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting image: " + imageId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete image");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * DELETE /api/spaces/{spaceId}/images - Eliminar todas las imágenes de un espacio
     * Solo ADMIN puede eliminar todas las imágenes
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteAllImages(@PathVariable UUID spaceId) {
        log.info("DELETE /api/spaces/{}/images - Deleting all images", spaceId);
        
        try {
            imageService.deleteAllImagesForSpace(spaceId);
            log.info("All images deleted successfully for space: {}", spaceId);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "All images deleted successfully");
            return ResponseEntity.ok(success);
            
        } catch (Exception e) {
            log.error("Error deleting all images for space: " + spaceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete images");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * PUT /api/spaces/{spaceId}/images/reorder - Reordenar imágenes
     * Solo ADMIN y SUPERVISOR pueden reordenar
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> reorderImages(@PathVariable UUID spaceId,
                                               @RequestBody List<Long> imageIds) {
        log.info("PUT /api/spaces/{}/images/reorder - Reordering images", spaceId);
        
        try {
            imageService.reorderImages(spaceId, imageIds);
            log.info("Images reordered successfully for space: {}", spaceId);
            
            Map<String, String> success = new HashMap<>();
            success.put("message", "Images reordered successfully");
            return ResponseEntity.ok(success);
            
        } catch (Exception e) {
            log.error("Error reordering images for space: " + spaceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reorder images");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
