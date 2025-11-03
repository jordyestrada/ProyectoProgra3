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
        space.setSpaceTypeId((short) 1); 
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

    
    @Transactional
    public SpaceDto createSpaceWithImages(SpaceDto spaceDto, List<MultipartFile> imageFiles) {
        log.info("Creating space with {} images: {}", imageFiles != null ? imageFiles.size() : 0, spaceDto.getName());
        
        
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
        
        
        List<SpaceImage> spaceImages = new ArrayList<>();
        
        if (imageFiles != null && !imageFiles.isEmpty()) {
            log.info("Uploading {} images to Cloudinary", imageFiles.size());
            
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                
                try {
                    
                    String imageUrl = cloudinaryService.uploadImageAndGetUrl(imageFile, "proyectoprogra");
                    
                    
                    SpaceImage spaceImage = new SpaceImage();
                    spaceImage.setSpaceId(spaceId);
                    spaceImage.setUrl(imageUrl);
                    spaceImage.setMain(i == 0); 
                    spaceImage.setOrd(i);
                    
                    spaceImages.add(spaceImage);
                    
                    log.info("Image {} uploaded successfully: {}", i + 1, imageUrl);
                    
                } catch (Exception e) {
                    log.error("Error uploading image {}: {}", i + 1, e.getMessage(), e);
                    
                }
            }
            
            
            if (!spaceImages.isEmpty()) {
                spaceImageRepository.saveAll(spaceImages);
                log.info("Saved {} images to database", spaceImages.size());
            }
        }
        
        
        return toDtoWithImages(savedSpace, spaceImages);
    }

    
    @Transactional
    public SpaceDto addImagesToSpace(UUID spaceId, List<MultipartFile> imageFiles) {
        log.info("Adding {} images to space {}", imageFiles.size(), spaceId);
        
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new RuntimeException("Space not found: " + spaceId));
        
        
        List<SpaceImage> existingImages = spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId);
        int currentMaxOrder = existingImages.isEmpty() ? -1 : 
                             existingImages.stream().mapToInt(SpaceImage::getOrd).max().orElse(-1);
        
        boolean hasMainImage = existingImages.stream().anyMatch(SpaceImage::isMain);
        
        List<SpaceImage> newImages = new ArrayList<>();
        
        for (int i = 0; i < imageFiles.size(); i++) {
            try {
                
                String imageUrl = cloudinaryService.uploadImageAndGetUrl(imageFiles.get(i), "proyectoprogra");
                
                SpaceImage spaceImage = new SpaceImage();
                spaceImage.setSpaceId(spaceId);
                spaceImage.setUrl(imageUrl);
                spaceImage.setMain(!hasMainImage && i == 0); 
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
        
        
        List<SpaceImage> allImages = spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId);
        return toDtoWithImages(space, allImages);
    }

    
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
        
        
        try {
            boolean deleted = cloudinaryService.deleteImageByUrl(image.getUrl());
            log.info("Image deleted from Cloudinary: {}", deleted);
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", e.getMessage(), e);
        }
        
        
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

    
    public List<SpaceDto> searchSpaces(String name, Integer spaceTypeId, Integer minCapacity, 
                                      Integer maxCapacity, String location, Boolean outdoor, Boolean activeOnly) {
        log.info("Performing advanced search with filters");
        
        return spaceRepository.findAll().stream()
                .filter(space -> {
                    
                    if (activeOnly && !space.isActive()) {
                        return false;
                    }
                    
                    
                    if (name != null && !name.trim().isEmpty()) {
                        String searchName = name.toLowerCase().trim();
                        if (!space.getName().toLowerCase().contains(searchName) && 
                            (space.getDescription() == null || !space.getDescription().toLowerCase().contains(searchName))) {
                            return false;
                        }
                    }
                    
                    
                    if (spaceTypeId != null && !spaceTypeId.equals((int) space.getSpaceTypeId())) {
                        return false;
                    }
                    
                    
                    if (minCapacity != null && space.getCapacity() < minCapacity) {
                        return false;
                    }
                    
                    
                    if (maxCapacity != null && space.getCapacity() > maxCapacity) {
                        return false;
                    }
                    
                    
                    if (location != null && !location.trim().isEmpty()) {
                        if (space.getLocation() == null || 
                            !space.getLocation().toLowerCase().contains(location.toLowerCase().trim())) {
                            return false;
                        }
                    }
                    
                    
                    if (outdoor != null && !outdoor.equals(space.isOutdoor())) {
                        return false;
                    }
                    
                    return true;
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<SpaceDto> findAvailableSpaces(String startDate, String endDate, 
                                            Integer spaceTypeId, Integer minCapacity) {
        log.info("Searching for available spaces from {} to {}", startDate, endDate);
        
        try {
            
            OffsetDateTime startsAt = OffsetDateTime.parse(startDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            OffsetDateTime endsAt = OffsetDateTime.parse(endDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            
            
            List<UUID> occupiedSpaceIds = reservationRepository.findOccupiedSpaceIds(startsAt, endsAt);
            log.info("Found {} occupied spaces in the date range", occupiedSpaceIds.size());
            
            return spaceRepository.findAll().stream()
                    .filter(space -> {
                        
                        if (!space.isActive()) {
                            return false;
                        }
                        
                        
                        if (occupiedSpaceIds.contains(space.getSpaceId())) {
                            return false;
                        }
                        
                        
                        if (spaceTypeId != null && !spaceTypeId.equals((int) space.getSpaceTypeId())) {
                            return false;
                        }
                        
                        
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

