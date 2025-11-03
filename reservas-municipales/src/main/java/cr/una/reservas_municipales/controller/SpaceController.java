package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.service.SpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SpaceController {

    private final SpaceService spaceService;

    /**
     * GET /api/spaces - Obtener todos los espacios
     */
    @GetMapping
    public ResponseEntity<List<SpaceDto>> getAllSpaces() {
        log.info("Fetching all spaces");
        try {
            List<SpaceDto> spaces = spaceService.listAll();
            log.info("Found {} spaces", spaces.size());
            return ResponseEntity.ok(spaces);
        } catch (Exception e) {
            log.error("Error fetching spaces", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/spaces/active - Obtener solo espacios activos
     */
    @GetMapping("/active")
    public ResponseEntity<List<SpaceDto>> getActiveSpaces() {
        log.info("Fetching active spaces");
        try {
            List<SpaceDto> activeSpaces = spaceService.listActiveSpaces();
            log.info("Found {} active spaces", activeSpaces.size());
            return ResponseEntity.ok(activeSpaces);
        } catch (Exception e) {
            log.error("Error fetching active spaces", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/spaces/search - Búsqueda avanzada de espacios
     * Parámetros de consulta opcionales para filtrar espacios
     */
    @GetMapping("/search")
    public ResponseEntity<List<SpaceDto>> searchSpaces(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer spaceTypeId,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean outdoor,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        
        log.info("Advanced search - name: {}, type: {}, minCap: {}, maxCap: {}, location: {}, outdoor: {}, activeOnly: {}", 
                name, spaceTypeId, minCapacity, maxCapacity, location, outdoor, activeOnly);
        
        try {
            List<SpaceDto> results = spaceService.searchSpaces(
                name, spaceTypeId, minCapacity, maxCapacity, location, outdoor, activeOnly);
            
            log.info("Search returned {} results", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in advanced search", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/spaces/available - Buscar espacios disponibles en un rango de tiempo
     */
    @GetMapping("/available")
    public ResponseEntity<List<SpaceDto>> getAvailableSpaces(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Integer spaceTypeId,
            @RequestParam(required = false) Integer minCapacity) {
        
        log.info("Searching available spaces from {} to {} with typeId: {}, minCapacity: {}", 
                startDate, endDate, spaceTypeId, minCapacity);
        
        try {
            List<SpaceDto> availableSpaces = spaceService.findAvailableSpaces(
                startDate, endDate, spaceTypeId, minCapacity);
            
            log.info("Found {} available spaces", availableSpaces.size());
            return ResponseEntity.ok(availableSpaces);
        } catch (Exception e) {
            log.error("Error searching available spaces", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/spaces/{id} - Obtener espacio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SpaceDto> getSpaceById(@PathVariable UUID id) {
        log.info("Fetching space with ID: {}", id);
        try {
            Optional<SpaceDto> space = spaceService.getById(id);
            if (space.isPresent()) {
                log.info("Found space: {}", space.get().getName());
                return ResponseEntity.ok(space.get());
            } else {
                log.warn("Space not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching space by ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/spaces - Crear nuevo espacio SIN imágenes (JSON)
     * Solo ADMIN y SUPERVISOR pueden crear espacios
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> createSpace(@Valid @RequestBody SpaceDto spaceDto) {
        log.info("Creating new space: {}", spaceDto.getName());
        try {
            // Validar que el nombre sea único
            if (spaceService.existsByName(spaceDto.getName())) {
                log.warn("Space name already exists: {}", spaceDto.getName());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Space name already exists");
                return ResponseEntity.badRequest().body(error);
            }

            SpaceDto createdSpace = spaceService.createSpace(spaceDto);
            log.info("Space created successfully with ID: {}", createdSpace.getSpaceId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSpace);
        } catch (Exception e) {
            log.error("Error creating space", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create space");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * POST /api/spaces/with-images - Crear espacio CON imágenes (multipart/form-data)
     * Solo ADMIN y SUPERVISOR pueden crear espacios
     */
    @PostMapping("/with-images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> createSpaceWithImages(
            @RequestParam("name") String name,
            @RequestParam("capacity") Integer capacity,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "outdoor", defaultValue = "false") boolean outdoor,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "images", required = false) List<org.springframework.web.multipart.MultipartFile> images) {
        
        log.info("Creating space with images: {}, images count: {}", name, images != null ? images.size() : 0);
        
        try {
            // Validar que el nombre sea único
            if (spaceService.existsByName(name)) {
                log.warn("Space name already exists: {}", name);
                Map<String, String> error = new HashMap<>();
                error.put("error", "Space name already exists");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Crear DTO del espacio
            SpaceDto spaceDto = new SpaceDto();
            spaceDto.setName(name);
            spaceDto.setCapacity(capacity);
            spaceDto.setLocation(location);
            spaceDto.setOutdoor(outdoor);
            spaceDto.setDescription(description);
            
            // Crear espacio con imágenes
            SpaceDto createdSpace = spaceService.createSpaceWithImages(spaceDto, images);
            log.info("Space with images created successfully: {}", createdSpace.getSpaceId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSpace);
            
        } catch (Exception e) {
            log.error("Error creating space with images", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create space with images");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * POST /api/spaces/{id}/images - Agregar imágenes a un espacio existente
     * Solo ADMIN y SUPERVISOR pueden agregar imágenes
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> addImagesToSpace(
            @PathVariable UUID id,
            @RequestParam("images") List<org.springframework.web.multipart.MultipartFile> images) {
        
        log.info("Adding {} images to space {}", images.size(), id);
        
        try {
            SpaceDto updatedSpace = spaceService.addImagesToSpace(id, images);
            log.info("Images added successfully to space {}", id);
            
            return ResponseEntity.ok(updatedSpace);
            
        } catch (RuntimeException e) {
            log.error("Error adding images to space {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Space not found");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("Error adding images to space {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add images");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * DELETE /api/spaces/{spaceId}/images/{imageId} - Eliminar una imagen específica
     * Solo ADMIN y SUPERVISOR pueden eliminar imágenes
     */
    @DeleteMapping("/{spaceId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> deleteSpaceImage(
            @PathVariable UUID spaceId,
            @PathVariable Long imageId) {
        
        log.info("Deleting image {} from space {}", imageId, spaceId);
        
        try {
            boolean deleted = spaceService.deleteSpaceImage(spaceId, imageId);
            
            if (deleted) {
                log.info("Image {} deleted from space {}", imageId, spaceId);
                Map<String, String> success = new HashMap<>();
                success.put("message", "Image deleted successfully");
                return ResponseEntity.ok(success);
            } else {
                log.warn("Image {} not found in space {}", imageId, spaceId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting image {} from space {}", imageId, spaceId, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete image");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * PUT /api/spaces/{id} - Actualizar espacio existente
     * Solo ADMIN y SUPERVISOR pueden actualizar espacios
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Object> updateSpace(@PathVariable UUID id, @Valid @RequestBody SpaceDto spaceDto) {
        log.info("Updating space with ID: {}", id);
        try {
            // Validar que el nombre sea único (excluyendo el espacio actual)
            if (spaceService.existsByNameAndNotId(spaceDto.getName(), id)) {
                log.warn("Space name already exists for different space: {}", spaceDto.getName());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Space name already exists");
                return ResponseEntity.badRequest().body(error);
            }

            Optional<SpaceDto> updatedSpace = spaceService.updateSpace(id, spaceDto);
            if (updatedSpace.isPresent()) {
                log.info("Space updated successfully: {}", id);
                return ResponseEntity.ok(updatedSpace.get());
            } else {
                log.warn("Space not found for update: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error updating space: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update space");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * DELETE /api/spaces/{id} - Desactivar espacio (soft delete)
     * Solo ADMIN puede desactivar espacios
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deactivateSpace(@PathVariable UUID id) {
        log.info("Deactivating space with ID: {}", id);
        try {
            boolean deactivated = spaceService.deactivateSpace(id);
            if (deactivated) {
                log.info("Space deactivated successfully: {}", id);
                Map<String, String> success = new HashMap<>();
                success.put("message", "Space deactivated successfully");
                return ResponseEntity.ok(success);
            } else {
                log.warn("Space not found for deactivation: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deactivating space: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate space");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * DELETE /api/spaces/{id}/permanent - Eliminar permanentemente
     * Solo ADMIN puede eliminar permanentemente (usar con cuidado)
     * NO permite eliminar si tiene reservas asociadas
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteSpacePermanent(@PathVariable UUID id) {
        log.info("Permanently deleting space with ID: {}", id);
        try {
            boolean deleted = spaceService.deleteSpace(id);
            if (deleted) {
                log.info("Space permanently deleted: {}", id);
                Map<String, String> success = new HashMap<>();
                success.put("message", "Space permanently deleted");
                return ResponseEntity.ok(success);
            } else {
                log.warn("Space not found for permanent deletion: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalStateException e) {
            // Error de validación: tiene reservas asociadas
            log.warn("Cannot delete space {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cannot delete space");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (Exception e) {
            log.error("Error permanently deleting space: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete space");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
