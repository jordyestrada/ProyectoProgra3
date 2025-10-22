package cr.una.reservas.frontend.ui.spaces;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller para la vista de detalles de un espacio
 * Muestra información completa, galería de imágenes, reseñas y disponibilidad
 */
public class SpaceDetailsController {
    
    // Image gallery
    @FXML private ImageView mainImage;
    @FXML private HBox thumbnailsContainer;
    
    // Space information
    @FXML private Label spaceNameLabel;
    @FXML private Label spaceTypeLabel;
    @FXML private Label capacityLabel;
    @FXML private Label priceLabel;
    @FXML private Label locationLabel;
    @FXML private Label ratingLabel;
    
    // Description
    @FXML private Label descriptionLabel;
    
    // Amenities
    @FXML private VBox amenitiesContainer;
    
    // Reviews
    @FXML private VBox reviewsContainer;
    @FXML private Label averageRatingLabel;
    @FXML private Label totalReviewsLabel;
    
    // Actions
    @FXML private Button reserveButton;
    @FXML private Button backButton;
    
    private Long spaceId;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ SpaceDetailsController inicializado");
    }
    
    /**
     * Establece el ID del espacio a mostrar
     */
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
        loadSpaceDetails();
    }
    
    /**
     * Carga los detalles del espacio desde la API
     */
    private void loadSpaceDetails() {
        System.out.println("📍 Cargando detalles del espacio ID: " + spaceId);
        
        // TODO: Llamar a GET /api/spaces/{id}
        // Space space = ApiClient.get("/api/spaces/" + spaceId, Space.class);
        
        // Mock data
        if (spaceNameLabel != null) {
            spaceNameLabel.setText("Salón de Eventos Municipal");
        }
        if (spaceTypeLabel != null) {
            spaceTypeLabel.setText("Salón de eventos");
        }
        if (capacityLabel != null) {
            capacityLabel.setText("200 personas");
        }
        if (priceLabel != null) {
            priceLabel.setText("₡150,000 / día");
        }
        if (locationLabel != null) {
            locationLabel.setText("Centro, Pérez Zeledón");
        }
        if (ratingLabel != null) {
            ratingLabel.setText("⭐ 4.8");
        }
        if (descriptionLabel != null) {
            descriptionLabel.setText("Amplio salón ideal para eventos corporativos...");
        }
        
        // Load images
        loadImages();
        
        // Load amenities
        loadAmenities();
        
        // Load reviews
        loadReviews();
    }
    
    /**
     * Carga las imágenes del espacio
     */
    private void loadImages() {
        System.out.println("🖼️ Cargando imágenes del espacio");
        // TODO: Cargar imágenes reales desde URLs
        // Por ahora usar placeholder
    }
    
    /**
     * Carga las amenidades del espacio
     */
    private void loadAmenities() {
        System.out.println("✨ Cargando amenidades");
        // TODO: Mostrar amenidades dinámicamente
    }
    
    /**
     * Carga las reseñas del espacio
     */
    private void loadReviews() {
        System.out.println("⭐ Cargando reseñas");
        // TODO: Llamar a GET /api/reviews/space/{spaceId}
    }
    
    /**
     * Maneja el botón "Reservar"
     */
    @FXML
    private void handleReserve() {
        System.out.println("📝 Navegando a formulario de reserva");
        navigateTo("/fxml/booking-form.fxml", "Nueva Reserva");
    }
    
    /**
     * Maneja el botón "Volver"
     */
    @FXML
    private void handleBack() {
        System.out.println("← Volviendo al catálogo");
        navigateTo("/fxml/spaces-catalog.fxml", "Catálogo de Espacios");
    }
    
    /**
     * Maneja clic en thumbnail de imagen
     */
    @FXML
    private void handleThumbnailClick(ImageView thumbnail) {
        if (mainImage != null && thumbnail != null) {
            mainImage.setImage(thumbnail.getImage());
        }
    }
    
    /**
     * Navega a otra pantalla
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) backButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + title);
        } catch (Exception e) {
            System.err.println("❌ Error al navegar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
