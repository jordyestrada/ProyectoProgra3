package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.SpaceSchedule;
import cr.una.reservas_municipales.repository.SpaceScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceScheduleService {
    private final SpaceScheduleRepository repository;

    public List<SpaceSchedule> listAll() {
        return repository.findAll();
    }
}
