package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.dto.SpaceImageDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceImageRepository spaceImageRepository;
    private final CloudinaryService cloudinaryService;

    public List<SpaceDto> listAll() {
        return spaceRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<SpaceDto> listActiveSpaces() {
        return spaceRepository.findAll().stream()
                .filter(Space::isActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<SpaceDto> getById(UUID id) {
        return spaceRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public SpaceDto createSpace(SpaceDto spaceDto) {
        log.info("Creating space: {}", spaceDto.getName());
        
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName(spaceDto.getName());
        space.setSpaceTypeId((short) 1); // Default type
        space.setCapacity(spaceDto.getCapacity());
        space.setLocation(spaceDto.getLocation());
        space.setOutdoor(spaceDto.isOutdoor());
        space.setActive(true);
        space.setDescription(spaceDto.getDescription());
        space.setCreatedAt(OffsetDateTime.now());
        space.setUpdatedAt(OffsetDateTime.now());

        Space saved = spaceRepository.save(space);
        return toDto(saved);
    }

    /**
     * Crear espacio con imágenes desde archivos MultipartFile
     */
    @Transactional
    public SpaceDto createSpaceWithImages(SpaceDto spaceDto, List<MultipartFile> imageFiles) {
        log.info("Creating space with {} images: {}", imageFiles != null ? imageFiles.size() : 0, spaceDto.getName());
        
        // 1. Crear el espacio primero
        Space space = new Space();
        UUID spaceId = UUID.randomUUID();
        space.setSpaceId(spaceId);
        space.setName(spaceDto.getName());
        space.setSpaceTypeId((short) 1);
        space.setCapacity(spaceDto.getCapacity());
        space.setLocation(spaceDto.getLocation());
        space.setOutdoor(spaceDto.isOutdoor());
        space.setActive(true);
        space.setDescription(spaceDto.getDescription());
        space.setCreatedAt(OffsetDateTime.now());
        space.setUpdatedAt(OffsetDateTime.now());
        
        Space savedSpace = spaceRepository.save(space);
        log.info("Space created with ID: {}", spaceId);
        
        // 2. Subir imágenes a Cloudinary y guardar solo URLs
        List<SpaceImage> spaceImages = new ArrayList<>();
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            log.info("Uploading {} images to Cloudinary", imageFiles.size());
            
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                
                try {
                    // Subir imagen a Cloudinary y obtener solo la URL
                    String imageUrl = cloudinaryService.uploadImageAndGetUrl(imageFile, "proyectoprogra");
                    
                    // Crear registro de imagen con solo la URL
                    SpaceImage spaceImage = new SpaceImage();
                    spaceImage.setSpaceId(spaceId);
                    spaceImage.setUrl(imageUrl);
                    spaceImage.setMain(i == 0); // Primera imagen es la principal
                    spaceImage.setOrd(i);
                    
                    spaceImages.add(spaceImage);
                    
                    log.info("Image {} uploaded successfully: {}", i + 1, imageUrl);
                    
                } catch (Exception e) {
                    log.error("Error uploading image {}: {}", i + 1, e.getMessage(), e);
                    // Continuar con las demás imágenes
                }
            }
            
            // Guardar todas las imágenes en la base de datos
            if (!spaceImages.isEmpty()) {
                spaceImageRepository.saveAll(spaceImages);
                log.info("Saved {} images to database", spaceImages.size());
            }
        }
        
        // 3. Retornar DTO con imágenes
        return toDtoWithImages(savedSpace, spaceImages);
    }

    /**
     * Agregar imágenes a un espacio existente
     */
    @Transactional
    public SpaceDto addImagesToSpace(UUID spaceId, List<MultipartFile> imageFiles) {
        log.info("Adding {} images to space {}", imageFiles.size(), spaceId);
        
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found: " + spaceId));
        
        // Obtener el orden máximo actual
        List<SpaceImage> existingImages = spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId);
        int currentMaxOrder = existingImages.isEmpty() ? -1 : 
                             existingImages.stream().mapToInt(SpaceImage::getOrd).max().orElse(-1);
        
        boolean hasMainImage = existingImages.stream().anyMatch(SpaceImage::isMain);
        
        List<SpaceImage> newImages = new ArrayList<>();
        
        for (int i = 0; i < imageFiles.size(); i++) {
            try {
                // Subir imagen y obtener solo la URL
                String imageUrl = cloudinaryService.uploadImageAndGetUrl(imageFiles.get(i), "proyectoprogra");
                
                SpaceImage spaceImage = new SpaceImage();
                spaceImage.setSpaceId(spaceId);
                spaceImage.setUrl(imageUrl);
                spaceImage.setMain(!hasMainImage && i == 0); // Primera nueva imagen es principal si no hay principal
                spaceImage.setOrd(currentMaxOrder + i + 1);
                
                newImages.add(spaceImage);
                
            } catch (Exception e) {
                log.error("Error uploading image: {}", e.getMessage(), e);
            }
        }
        
        if (!newImages.isEmpty()) {
            spaceImageRepository.saveAll(newImages);
            log.info("Added {} images to space {}", newImages.size(), spaceId);
        }
        
        // Retornar con todas las imágenes
        List<SpaceImage> allImages = spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId);
        return toDtoWithImages(space, allImages);
    }

    /**
     * Eliminar una imagen específica de un espacio
     */
    @Transactional
    public boolean deleteSpaceImage(UUID spaceId, Long imageId) {
        log.info("Deleting image {} from space {}", imageId, spaceId);
        
        Optional<SpaceImage> imageOpt = spaceImageRepository.findById(imageId);
        
        if (imageOpt.isEmpty()) {
            log.warn("Image not found: {}", imageId);
            return false;
        }
        
        SpaceImage image = imageOpt.get();
        
        if (!image.getSpaceId().equals(spaceId)) {
            log.warn("Image {} does not belong to space {}", imageId, spaceId);
            return false;
        }
        
        // Eliminar de Cloudinary
        try {
            boolean deleted = cloudinaryService.deleteImageByUrl(image.getUrl());
            log.info("Image deleted from Cloudinary: {}", deleted);
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage(), e);
        }
        
        // Eliminar de base de datos
        spaceImageRepository.delete(image);
        log.info("Image {} deleted from database", imageId);
        
        return true;
    }

    @Transactional
    public Optional<SpaceDto> updateSpace(UUID id, SpaceDto spaceDto) {
        log.info("Updating space: {}", id);
        
        return spaceRepository.findById(id).map(space -> {
            space.setName(spaceDto.getName());
            space.setCapacity(spaceDto.getCapacity());
            space.setLocation(spaceDto.getLocation());
            space.setOutdoor(spaceDto.isOutdoor());
            space.setActive(spaceDto.isActive());
            space.setDescription(spaceDto.getDescription());
            space.setUpdatedAt(OffsetDateTime.now());
            
            return toDto(spaceRepository.save(space));
        });
    }

    @Transactional
    public boolean deactivateSpace(UUID id) {
        return spaceRepository.findById(id).map(space -> {
            space.setActive(false);
            space.setUpdatedAt(OffsetDateTime.now());
            spaceRepository.save(space);
            return true;
        }).orElse(false);
    }

    @Transactional
    public boolean deleteSpace(UUID id) {
        if (!spaceRepository.existsById(id)) {
            return false;
        }
        
        // Validar que no tenga reservas asociadas
        long reservationCount = reservationRepository.countBySpaceId(id);
        if (reservationCount > 0) {
            log.warn("Cannot delete space {} - has {} associated reservations", id, reservationCount);
            throw new IllegalStateException(
                String.format("Cannot delete space: it has %d associated reservation(s). Please deactivate instead.", reservationCount)
            );
        }
        
        log.info("Permanently deleting space {} (no reservations found)", id);
        spaceRepository.deleteById(id);
        return true;
    }

    public boolean existsByName(String name) {
        return spaceRepository.findAll().stream()
                .anyMatch(space -> space.getName().equalsIgnoreCase(name));
    }

    public boolean existsByNameAndNotId(String name, UUID excludeId) {
        return spaceRepository.findAll().stream()
                .anyMatch(space -> space.getName().equalsIgnoreCase(name) && 
                                 !space.getSpaceId().equals(excludeId));
    }

    /**
     * Búsqueda avanzada de espacios con múltiples filtros
     */
    public List<SpaceDto> searchSpaces(String name, Integer spaceTypeId, Integer minCapacity, 
                                      Integer maxCapacity, String location, Boolean outdoor, Boolean activeOnly) {
        log.info("Performing advanced search with filters");
        
        return spaceRepository.findAll().stream()
                .filter(space -> {
                    // Filtro por estado activo
                    if (activeOnly && !space.isActive()) {
                        return false;
                    }
                    
                    // Filtro por nombre (búsqueda parcial, case-insensitive)
                    if (name != null && !name.trim().isEmpty()) {
                        String searchName = name.toLowerCase().trim();
                        if (!space.getName().toLowerCase().contains(searchName) && 
                            (space.getDescription() == null || !space.getDescription().toLowerCase().contains(searchName))) {
                            return false;
                        }
                    }
                    
                    // Filtro por tipo de espacio
                    if (spaceTypeId != null && !spaceTypeId.equals((int) space.getSpaceTypeId())) {
                        return false;
                    }
                    
                    // Filtro por capacidad mínima
                    if (minCapacity != null && space.getCapacity() < minCapacity) {
                        return false;
                    }
                    
                    // Filtro por capacidad máxima
                    if (maxCapacity != null && space.getCapacity() > maxCapacity) {
                        return false;
                    }
                    
                    // Filtro por ubicación (búsqueda parcial)
                    if (location != null && !location.trim().isEmpty()) {
                        if (space.getLocation() == null || 
                            !space.getLocation().toLowerCase().contains(location.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    
                    // Filtro por exterior/interior
                    if (outdoor != null && !outdoor.equals(space.isOutdoor())) {
                        return false;
                    }
                    
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Buscar espacios disponibles en un rango de tiempo
     */
    public List<SpaceDto> findAvailableSpaces(String startDate, String endDate, 
                                            Integer spaceTypeId, Integer minCapacity) {
        log.info("Searching for available spaces from {} to {}", startDate, endDate);
        
        try {
            // Parsear las fechas
            OffsetDateTime startsAt = OffsetDateTime.parse(startDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endsAt = OffsetDateTime.parse(endDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            
            // Obtener IDs de espacios ocupados en el rango de fechas
            List<UUID> occupiedSpaceIds = reservationRepository.findOccupiedSpaceIds(startsAt, endsAt);
            log.info("Found {} occupied spaces in the date range", occupiedSpaceIds.size());
            
            return spaceRepository.findAll().stream()
                    .filter(space -> {
                        // Filtro por estado activo
                        if (!space.isActive()) {
                            return false;
                        }
                        
                        // Verificar que el espacio NO esté ocupado
                        if (occupiedSpaceIds.contains(space.getSpaceId())) {
                            return false;
                        }
                        
                        // Filtro por tipo de espacio
                        if (spaceTypeId != null && !spaceTypeId.equals((int) space.getSpaceTypeId())) {
                            return false;
                        }
                        
                        // Filtro por capacidad mínima
                        if (minCapacity != null && space.getCapacity() < minCapacity) {
                            return false;
                        }
                        
                        return true;
                    })
                    .map(this::toDto)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error parsing dates or searching available spaces", e);
            throw new RuntimeException("Invalid date format or search error", e);
        }
    }

    private SpaceDto toDto(Space s) {
        SpaceDto d = new SpaceDto();
        d.setSpaceId(s.getSpaceId());
        d.setName(s.getName());
        d.setCapacity(s.getCapacity());
        d.setLocation(s.getLocation());
        d.setOutdoor(s.isOutdoor());
        d.setActive(s.isActive());
        d.setDescription(s.getDescription());
        
        // Cargar imágenes si existen
        List<SpaceImage> images = spaceImageRepository.findBySpaceIdOrderByOrdAsc(s.getSpaceId());
        d.setImages(images.stream().map(this::imageToDto).collect(Collectors.toList()));
        
        return d;
    }

    private SpaceDto toDtoWithImages(Space s, List<SpaceImage> images) {
        SpaceDto d = new SpaceDto();
        d.setSpaceId(s.getSpaceId());
        d.setName(s.getName());
        d.setCapacity(s.getCapacity());
        d.setLocation(s.getLocation());
        d.setOutdoor(s.isOutdoor());
        d.setActive(s.isActive());
        d.setDescription(s.getDescription());
        d.setImages(images.stream().map(this::imageToDto).collect(Collectors.toList()));
        return d;
    }

    private SpaceImageDto imageToDto(SpaceImage img) {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setImageId(img.getImageId());
        dto.setUrl(img.getUrl());
        dto.setMain(img.isMain());
        dto.setOrd(img.getOrd());
        dto.setCreatedAt(img.getCreatedAt());
        return dto;
    }
}

