package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceRateDto;
import cr.una.reservas_municipales.model.SpaceRate;
import cr.una.reservas_municipales.repository.SpaceRateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpaceRateService {
    private final SpaceRateRepository spaceRateRepository;

    public List<SpaceRateDto> listAll() {
        return spaceRateRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<SpaceRateDto> getById(Long id) {
        return spaceRateRepository.findById(id).map(this::toDto);
    }

    private SpaceRateDto toDto(SpaceRate r) {
        SpaceRateDto d = new SpaceRateDto();
        d.setRateId(r.getRateId());
        d.setSpaceId(r.getSpaceId());
        d.setName(r.getName());
        d.setUnit(r.getUnit());
        d.setBlockMinutes(r.getBlockMinutes());
        d.setPrice(r.getPrice());
        d.setCurrency(r.getCurrency());
        d.setAppliesFrom(r.getAppliesFrom());
        d.setAppliesTo(r.getAppliesTo());
        d.setActive(r.isActive());
        return d;
    }
}
