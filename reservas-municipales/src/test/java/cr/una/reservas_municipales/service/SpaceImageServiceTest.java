package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.SpaceImageRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceImageServiceTest {

    @Mock
    private SpaceImageRepository imageRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private SpaceImageService service;

    private UUID spaceId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    }

    // listAll
    @Test
    void listAll_returnsAll() {
        when(imageRepository.findAll()).thenReturn(List.of(img(1L), img(2L)));

        var result = service.listAll();

        assertThat(result).hasSize(2).extracting(SpaceImage::getImageId).containsExactly(1L, 2L);
        verify(imageRepository).findAll();
    }

    // getImagesBySpace
    @Test
    void getImagesBySpace_returnsOrdered() {
        SpaceImage a = img(1L); a.setOrd(1); a.setSpaceId(spaceId);
        SpaceImage b = img(2L); b.setOrd(2); b.setSpaceId(spaceId);
        when(imageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(List.of(a, b));

        var result = service.getImagesBySpace(spaceId);

        assertThat(result).containsExactly(a, b);
        verify(imageRepository).findBySpaceIdOrderByOrdAsc(spaceId);
    }

    // getImageById
    @Test
    void getImageById_found() {
        when(imageRepository.findById(10L)).thenReturn(Optional.of(img(10L)));

        var result = service.getImageById(10L);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getImageId()).isEqualTo(10L);
        verify(imageRepository).findById(10L);
    }

    @Test
    void getImageById_notFound() {
        when(imageRepository.findById(10L)).thenReturn(Optional.empty());

        var result = service.getImageById(10L);

        assertThat(result).isEmpty();
        verify(imageRepository).findById(10L);
    }

    // addImage
    @Test
    void addImage_spaceNotFound_throws() {
        when(spaceRepository.existsById(spaceId)).thenReturn(false);

        assertThatThrownBy(() -> service.addImage(spaceId, "http://x", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Space not found");
        verify(spaceRepository).existsById(spaceId);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void addImage_firstImage_autoMainTrue() {
        when(spaceRepository.existsById(spaceId)).thenReturn(true);
        when(imageRepository.countBySpaceId(spaceId)).thenReturn(0L);
        // capture the saved image to verify fields
        ArgumentCaptor<SpaceImage> captor = ArgumentCaptor.forClass(SpaceImage.class);
        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> {
            SpaceImage si = inv.getArgument(0);
            if (si.getImageId() == null) si.setImageId(100L);
            return si;
        });

        SpaceImage saved = service.addImage(spaceId, "http://img/1.jpg", false);

        verify(imageRepository).save(captor.capture());
        SpaceImage toSave = captor.getValue();
        assertThat(toSave.getSpaceId()).isEqualTo(spaceId);
        assertThat(toSave.getUrl()).isEqualTo("http://img/1.jpg");
        assertThat(toSave.isMain()).isTrue(); // first image becomes main
        assertThat(toSave.getOrd()).isEqualTo(1);
        assertThat(toSave.getCreatedAt()).isNotNull();

        assertThat(saved.getImageId()).isEqualTo(100L);
    }

    @Test
    void addImage_isMainTrue_clearsOthersAndSetsNewMain() {
        when(spaceRepository.existsById(spaceId)).thenReturn(true);
        when(imageRepository.countBySpaceId(spaceId)).thenReturn(2L);

        SpaceImage ex1 = new SpaceImage(); ex1.setImageId(1L); ex1.setSpaceId(spaceId); ex1.setMain(true);
        SpaceImage ex2 = new SpaceImage(); ex2.setImageId(2L); ex2.setSpaceId(spaceId); ex2.setMain(false);
        when(imageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(List.of(ex1, ex2));

        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> {
            SpaceImage si = inv.getArgument(0);
            if (si.getImageId() == null) si.setImageId(200L);
            return si;
        });

        SpaceImage saved = service.addImage(spaceId, "http://img/new.jpg", true);

        // existing images must be saved with main=false
        assertThat(ex1.isMain()).isFalse();
        assertThat(ex2.isMain()).isFalse();
        verify(imageRepository, times(1)).findBySpaceIdOrderByOrdAsc(spaceId);
        verify(imageRepository, atLeast(2)).save(any(SpaceImage.class));
        assertThat(saved.isMain()).isTrue();
        assertThat(saved.getOrd()).isEqualTo(3);
    }

    @Test
    void addImage_nonFirst_andNotMain_keepsMainFalse() {
        when(spaceRepository.existsById(spaceId)).thenReturn(true);
        when(imageRepository.countBySpaceId(spaceId)).thenReturn(5L);
        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceImage saved = service.addImage(spaceId, "http://img/n.jpg", false);

        assertThat(saved.isMain()).isFalse();
        assertThat(saved.getOrd()).isEqualTo(6);
        verify(imageRepository).countBySpaceId(spaceId);
    }

    // updateImage
    @Test
    void updateImage_notFound_returnsEmpty() {
        when(imageRepository.findById(7L)).thenReturn(Optional.empty());

        var result = service.updateImage(7L, "u", true);
        assertThat(result).isEmpty();
    }

    @Test
    void updateImage_updateUrlOnly() {
        SpaceImage current = new SpaceImage();
        current.setImageId(10L); current.setSpaceId(spaceId); current.setUrl("http://old"); current.setMain(false);
        when(imageRepository.findById(10L)).thenReturn(Optional.of(current));
        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.updateImage(10L, "http://new", null);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getUrl()).isEqualTo("http://new");
        assertThat(result.orElseThrow().isMain()).isFalse();
        verify(imageRepository, never()).findBySpaceIdOrderByOrdAsc(any());
        verify(imageRepository).save(current);
    }

    @Test
    void updateImage_setMainTrue_clearsOthers() {
        SpaceImage current = new SpaceImage();
        current.setImageId(10L); current.setSpaceId(spaceId); current.setUrl("http://u"); current.setMain(false);
        SpaceImage other1 = new SpaceImage(); other1.setImageId(11L); other1.setSpaceId(spaceId); other1.setMain(true);
        SpaceImage other2 = new SpaceImage(); other2.setImageId(12L); other2.setSpaceId(spaceId); other2.setMain(false);

        when(imageRepository.findById(10L)).thenReturn(Optional.of(current));
        when(imageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(List.of(other1, other2));
        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.updateImage(10L, null, true);

        assertThat(result).isPresent();
        assertThat(current.isMain()).isTrue();
        assertThat(other1.isMain()).isFalse();
        assertThat(other2.isMain()).isFalse();
        verify(imageRepository).findBySpaceIdOrderByOrdAsc(spaceId);
        verify(imageRepository, atLeast(3)).save(any(SpaceImage.class));
    }

    @Test
    void updateImage_setMainFalse_onlyCurrentUpdated() {
        SpaceImage current = new SpaceImage();
        current.setImageId(10L); current.setSpaceId(spaceId); current.setUrl("http://u"); current.setMain(true);

        when(imageRepository.findById(10L)).thenReturn(Optional.of(current));
        when(imageRepository.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.updateImage(10L, null, false);

        assertThat(result).isPresent();
        assertThat(current.isMain()).isFalse();
        verify(imageRepository, never()).findBySpaceIdOrderByOrdAsc(any());
        verify(imageRepository).save(current);
    }

    // deleteImage
    @Test
    void deleteImage_notFound_returnsFalse() {
        when(imageRepository.findById(99L)).thenReturn(Optional.empty());

        boolean ok = service.deleteImage(99L);
        assertThat(ok).isFalse();
        verify(imageRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteImage_nonMain_simpleDelete() {
        SpaceImage current = new SpaceImage(); current.setImageId(5L); current.setSpaceId(spaceId); current.setMain(false);
        when(imageRepository.findById(5L)).thenReturn(Optional.of(current));

        boolean ok = service.deleteImage(5L);

        assertThat(ok).isTrue();
        verify(imageRepository).deleteById(5L);
        verify(imageRepository, never()).findBySpaceIdOrderByOrdAsc(spaceId);
    }

    @Test
    void deleteImage_main_setsNewMainWhenRemaining() {
        SpaceImage current = new SpaceImage(); current.setImageId(5L); current.setSpaceId(spaceId); current.setMain(true);
        SpaceImage rem1 = new SpaceImage(); rem1.setImageId(6L); rem1.setSpaceId(spaceId); rem1.setMain(false);
        SpaceImage rem2 = new SpaceImage(); rem2.setImageId(7L); rem2.setSpaceId(spaceId); rem2.setMain(false);

        when(imageRepository.findById(5L)).thenReturn(Optional.of(current));
        when(imageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(List.of(rem1, rem2));

        boolean ok = service.deleteImage(5L);

        assertThat(ok).isTrue();
        verify(imageRepository).deleteById(5L);
        assertThat(rem1.isMain()).isTrue();
        verify(imageRepository).save(rem1);
    }

    @Test
    void deleteImage_main_noRemaining() {
        SpaceImage current = new SpaceImage(); current.setImageId(5L); current.setSpaceId(spaceId); current.setMain(true);
        when(imageRepository.findById(5L)).thenReturn(Optional.of(current));
        when(imageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(Collections.emptyList());

        boolean ok = service.deleteImage(5L);
        assertThat(ok).isTrue();
        verify(imageRepository).deleteById(5L);
        verify(imageRepository, never()).save(any());
    }

    // deleteAllImagesForSpace
    @Test
    void deleteAllImagesForSpace_deletesBySpace() {
        service.deleteAllImagesForSpace(spaceId);
        verify(imageRepository).deleteBySpaceId(spaceId);
    }

    // reorderImages
    @Test
    void reorderImages_updatesOrderOnlyForMatchingSpaceAndExistingImages() {
        UUID otherSpace = UUID.fromString("22222222-2222-2222-2222-222222222222");
        SpaceImage imgA = new SpaceImage(); imgA.setImageId(1L); imgA.setSpaceId(spaceId); imgA.setOrd(5);
        SpaceImage imgB = new SpaceImage(); imgB.setImageId(2L); imgB.setSpaceId(otherSpace); imgB.setOrd(6);
        SpaceImage imgC = new SpaceImage(); imgC.setImageId(3L); imgC.setSpaceId(spaceId); imgC.setOrd(7);

        when(imageRepository.findById(1L)).thenReturn(Optional.of(imgA));
        when(imageRepository.findById(2L)).thenReturn(Optional.of(imgB)); // mismatched space should be skipped
        when(imageRepository.findById(3L)).thenReturn(Optional.of(imgC));
        when(imageRepository.findById(999L)).thenReturn(Optional.empty()); // non-existing id is ignored

        service.reorderImages(spaceId, List.of(1L, 2L, 3L, 999L));

        // Only imgA and imgC should be updated and saved with new ord values (1 and 3)
        assertThat(imgA.getOrd()).isEqualTo(1);
        assertThat(imgC.getOrd()).isEqualTo(3);
        // imgB retains its ord since space doesn't match
        assertThat(imgB.getOrd()).isEqualTo(6);
        verify(imageRepository).save(imgA);
        verify(imageRepository).save(imgC);
        verify(imageRepository, never()).save(eq(imgB));
    }

    private static SpaceImage img(Long id) {
        SpaceImage si = new SpaceImage();
        si.setImageId(id);
        si.setSpaceId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        si.setUrl("http://img/" + id + ".jpg");
        si.setMain(false);
        si.setOrd(1);
        si.setCreatedAt(OffsetDateTime.now());
        return si;
    }
}

