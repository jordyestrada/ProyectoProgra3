package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;
    private final ReservationRepository reservationRepository;

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
        return d;
    }
}

