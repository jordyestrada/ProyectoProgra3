package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;

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
        if (spaceRepository.existsById(id)) {
            spaceRepository.deleteById(id);
            return true;
        }
        return false;
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

