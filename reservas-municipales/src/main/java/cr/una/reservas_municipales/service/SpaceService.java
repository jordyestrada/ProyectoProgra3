package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceService {
    private final SpaceRepository spaceRepository;

    public List<SpaceDto> listAll() {
        return spaceRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<SpaceDto> getById(UUID id) {
        return spaceRepository.findById(id).map(this::toDto);
    }

    private SpaceDto toDto(Space s) {
        SpaceDto d = new SpaceDto();
        d.setSpaceId(s.getSpaceId());
        d.setName(s.getName());
        d.setCapacity(s.getCapacity());
        d.setLocation(s.getLocation());
        d.setOutdoor(s.isOutdoor());
        d.setActive(s.isActive());
        return d;
    }
}

