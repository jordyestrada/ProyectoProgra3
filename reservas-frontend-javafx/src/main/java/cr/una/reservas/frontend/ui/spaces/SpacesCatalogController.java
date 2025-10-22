package cr.una.reservas.frontend.ui.spaces;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Controller para el catálogo de espacios
 * Muestra la lista de espacios disponibles con búsqueda y filtros
 */
public class SpacesCatalogController {
    
    // Search and filters
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> capacityFilter;
    @FXML private Button resetFiltersButton;
    
    // Content
    @FXML private GridPane spacesGrid;
    @FXML private ScrollPane scrollPane;
    
    // Navigation
    @FXML private Button dashboardButton;
    @FXML private Button logoutButton;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ SpacesCatalogController inicializado");
        
        // Configurar filtros
        setupFilters();
        
        // Cargar espacios
        loadSpaces();
        
        // Configurar búsqueda al presionar Enter
        if (searchField != null) {
            searchField.setOnAction(event -> handleSearch());
        }
    }
    
    /**
     * Configura los ComboBox de filtros
     */
    private void setupFilters() {
        if (typeFilter != null) {
            typeFilter.getItems().addAll(
                "Todos los tipos",
                "Salón de eventos",
                "Auditorio",
                "Cancha deportiva",
                "Sala de reuniones",
                "Parque"
            );
            typeFilter.setValue("Todos los tipos");
        }
        
        if (capacityFilter != null) {
            capacityFilter.getItems().addAll(
                "Todas las capacidades",
                "Hasta 50 personas",
                "51 - 100 personas",
                "101 - 200 personas",
                "Más de 200 personas"
            );
            capacityFilter.setValue("Todas las capacidades");
        }
    }
    
    /**
     * Carga los espacios desde la API
     */
    private void loadSpaces() {
        System.out.println("📍 Cargando espacios...");
        
        // TODO: Llamar a GET /api/spaces/active
        // Por ahora, mostrar mensaje
        if (spacesGrid != null) {
            spacesGrid.getChildren().clear();
            // Aquí se agregarán las cards de espacios dinámicamente
        }
        
        System.out.println("✅ Espacios cargados (mock)");
    }
    
    /**
     * Maneja el botón de búsqueda
     */
    @FXML
    private void handleSearch() {
        String query = searchField != null ? searchField.getText() : "";
        System.out.println("🔍 Buscando: " + query);
        
        // TODO: Filtrar espacios por query
        loadSpacesWithFilters(query, null, null);
    }
    
    /**
     * Maneja cambios en el filtro de tipo
     */
    @FXML
    private void handleTypeFilter() {
        String type = typeFilter != null ? typeFilter.getValue() : null;
        System.out.println("🏷️ Filtro tipo: " + type);
        applyFilters();
    }
    
    /**
     * Maneja cambios en el filtro de capacidad
     */
    @FXML
    private void handleCapacityFilter() {
        String capacity = capacityFilter != null ? capacityFilter.getValue() : null;
        System.out.println("👥 Filtro capacidad: " + capacity);
        applyFilters();
    }
    
    /**
     * Resetea todos los filtros
     */
    @FXML
    private void handleResetFilters() {
        System.out.println("🔄 Reseteando filtros");
        
        if (searchField != null) {
            searchField.clear();
        }
        if (typeFilter != null) {
            typeFilter.setValue("Todos los tipos");
        }
        if (capacityFilter != null) {
            capacityFilter.setValue("Todas las capacidades");
        }
        
        loadSpaces();
    }
    
    /**
     * Aplica los filtros actuales
     */
    private void applyFilters() {
        String query = searchField != null ? searchField.getText() : "";
        String type = typeFilter != null ? typeFilter.getValue() : null;
        String capacity = capacityFilter != null ? capacityFilter.getValue() : null;
        
        loadSpacesWithFilters(query, type, capacity);
    }
    
    /**
     * Carga espacios con filtros aplicados
     */
    private void loadSpacesWithFilters(String query, String type, String capacity) {
        System.out.println("📍 Cargando espacios con filtros:");
        System.out.println("   Query: " + query);
        System.out.println("   Tipo: " + type);
        System.out.println("   Capacidad: " + capacity);
        
        // TODO: Llamar a API con parámetros de filtro
        // GET /api/spaces?search={query}&type={type}&capacity={capacity}
    }
    
    /**
     * Navega a los detalles de un espacio
     */
    private void navigateToSpaceDetails(Long spaceId) {
        System.out.println("📍 Navegando a detalles del espacio ID: " + spaceId);
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/space-details.fxml")
            );
            javafx.scene.Parent root = loader.load();
            
            // TODO: Pasar el spaceId al controller de detalles
            // SpaceDetailsController controller = loader.getController();
            // controller.setSpaceId(spaceId);
            
            javafx.stage.Stage stage = (javafx.stage.Stage) spacesGrid.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - Detalle del Espacio");
            
        } catch (Exception e) {
            System.err.println("❌ Error al navegar a detalles: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navega al dashboard
     */
    @FXML
    private void handleDashboard() {
        navigateTo("/fxml/dashboard.fxml", "Dashboard");
    }
    
    /**
     * Navega a otra pantalla
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) searchButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + title);
        } catch (Exception e) {
            System.err.println("❌ Error al navegar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
