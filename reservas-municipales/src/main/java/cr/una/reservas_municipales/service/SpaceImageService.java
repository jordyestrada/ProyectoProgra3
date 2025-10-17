package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.SpaceImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceImageService {
    private final SpaceImageRepository repository;

    public List<SpaceImage> listAll() {
        return repository.findAll();
    }
}
