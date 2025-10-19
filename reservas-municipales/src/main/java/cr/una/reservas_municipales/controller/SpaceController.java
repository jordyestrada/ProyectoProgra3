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
     * POST /api/spaces - Crear nuevo espacio
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
        } catch (Exception e) {
            log.error("Error permanently deleting space: {}", id, e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete space");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
