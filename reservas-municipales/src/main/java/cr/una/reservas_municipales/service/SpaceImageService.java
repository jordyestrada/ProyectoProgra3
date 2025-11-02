package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.SpaceImageRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceImageService {
    private final SpaceImageRepository repository;
    private final SpaceRepository spaceRepository;

    public List<SpaceImage> listAll() {
        return repository.findAll();
    }

    public List<SpaceImage> getImagesBySpace(UUID spaceId) {
        log.info("Getting images for space: {}", spaceId);
        return repository.findBySpaceIdOrderByOrdAsc(spaceId);
    }

    public Optional<SpaceImage> getImageById(Long imageId) {
        return repository.findById(imageId);
    }

    @Transactional
    public SpaceImage addImage(UUID spaceId, String url, boolean isMain) {
        log.info("Adding image to space: {}", spaceId);
        
        // Verificar que el espacio existe
        if (!spaceRepository.existsById(spaceId)) {
            throw new IllegalArgumentException("Space not found with ID: " + spaceId);
        }

        // Obtener el siguiente orden
        long imageCount = repository.countBySpaceId(spaceId);
        
        // Si esta será la imagen principal, quitar el flag de las demás
        if (isMain) {
            List<SpaceImage> existingImages = repository.findBySpaceIdOrderByOrdAsc(spaceId);
            existingImages.forEach(img -> {
                img.setMain(false);
                repository.save(img);
            });
        }

        SpaceImage image = new SpaceImage();
        image.setSpaceId(spaceId);
        image.setUrl(url);
        image.setMain(isMain || imageCount == 0); // Primera imagen siempre es main
        image.setOrd((int) (imageCount + 1));
        image.setCreatedAt(OffsetDateTime.now());

        return repository.save(image);
    }

    @Transactional
    public Optional<SpaceImage> updateImage(Long imageId, String url, Boolean isMain) {
        log.info("Updating image: {}", imageId);
        
        return repository.findById(imageId).map(image -> {
            if (url != null) {
                image.setUrl(url);
            }
            
            if (isMain != null && isMain) {
                // Quitar flag main de otras imágenes del mismo espacio
                List<SpaceImage> spaceImages = repository.findBySpaceIdOrderByOrdAsc(image.getSpaceId());
                spaceImages.forEach(img -> {
                    if (!img.getImageId().equals(imageId)) {
                        img.setMain(false);
                        repository.save(img);
                    }
                });
                image.setMain(true);
            } else if (isMain != null) {
                image.setMain(false);
            }
            
            return repository.save(image);
        });
    }

    @Transactional
    public boolean deleteImage(Long imageId) {
        log.info("Deleting image: {}", imageId);
        
        Optional<SpaceImage> imageOpt = repository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return false;
        }

        SpaceImage image = imageOpt.get();
        UUID spaceId = image.getSpaceId();
        boolean wasMain = image.isMain();
        
        repository.deleteById(imageId);
        
        // Si era la imagen principal, establecer otra como principal
        if (wasMain) {
            List<SpaceImage> remainingImages = repository.findBySpaceIdOrderByOrdAsc(spaceId);
            if (!remainingImages.isEmpty()) {
                SpaceImage newMain = remainingImages.get(0);
                newMain.setMain(true);
                repository.save(newMain);
                log.info("New main image set: {}", newMain.getImageId());
            }
        }
        
        return true;
    }

    @Transactional
    public void deleteAllImagesForSpace(UUID spaceId) {
        log.info("Deleting all images for space: {}", spaceId);
        repository.deleteBySpaceId(spaceId);
    }

    @Transactional
    public void reorderImages(UUID spaceId, List<Long> imageIds) {
        log.info("Reordering images for space: {}", spaceId);
        
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            final int newOrder = i + 1; // Make it effectively final
            repository.findById(imageId).ifPresent(image -> {
                if (image.getSpaceId().equals(spaceId)) {
                    image.setOrd(newOrder);
                    repository.save(image);
                }
            });
        }
    }
}