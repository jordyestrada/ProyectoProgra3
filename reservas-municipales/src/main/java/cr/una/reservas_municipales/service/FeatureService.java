package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Feature;
import cr.una.reservas_municipales.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureService {
    private final FeatureRepository featureRepository;

    public List<Feature> listAll() {
        return featureRepository.findAll();
    }
}
