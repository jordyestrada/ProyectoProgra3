package cr.una.reservas.frontend.ui.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Controller para la pantalla de administración de espacios
 * Permite CRUD completo de espacios (Create, Read, Update, Delete)
 */
public class AdminSpacesController {
    
    // Search and filters
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> typeFilter;
    
    // Table
    @FXML private TableView<Object> spacesTable;
    @FXML private TableColumn<Object, Long> idColumn;
    @FXML private TableColumn<Object, String> nameColumn;
    @FXML private TableColumn<Object, String> typeColumn;
    @FXML private TableColumn<Object, Integer> capacityColumn;
    @FXML private TableColumn<Object, Double> priceColumn;
    @FXML private TableColumn<Object, String> statusColumn;
    @FXML private TableColumn<Object, Void> actionsColumn;
    
    // Actions
    @FXML private Button createButton;
    @FXML private Button refreshButton;
    
    // Pagination
    @FXML private Pagination pagination;
    
    private ObservableList<Object> spacesData = FXCollections.observableArrayList();
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ AdminSpacesController inicializado");
        
        // Configurar tabla
        setupTable();
        
        // Configurar filtros
        setupFilters();
        
        // Cargar espacios
        loadSpaces();
    }
    
    /**
     * Configura la tabla de espacios
     */
    private void setupTable() {
        if (spacesTable != null) {
            // Configurar columnas
            if (idColumn != null) {
                idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            }
            if (nameColumn != null) {
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            }
            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
            }
            if (capacityColumn != null) {
                capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
            }
            if (priceColumn != null) {
                priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
            }
            if (statusColumn != null) {
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            }
            
            // Configurar columna de acciones
            if (actionsColumn != null) {
                addActionsColumn();
            }
            
            spacesTable.setItems(spacesData);
        }
    }
    
    /**
     * Agrega botones de acción a cada fila
     */
    private void addActionsColumn() {
        // TODO: Agregar botones Edit y Delete en cada fila
        System.out.println("⚙️ Configurando columna de acciones");
    }
    
    /**
     * Configura los filtros
     */
    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("Todos", "Activo", "Inactivo");
            statusFilter.setValue("Todos");
        }
        
        if (typeFilter != null) {
            typeFilter.getItems().addAll(
                "Todos",
                "Salón de eventos",
                "Auditorio",
                "Cancha deportiva",
                "Sala de reuniones"
            );
            typeFilter.setValue("Todos");
        }
    }
    
    /**
     * Carga los espacios desde la API
     */
    private void loadSpaces() {
        System.out.println("📍 Cargando espacios...");
        
        // TODO: Llamar a GET /api/spaces
        // List<Space> spaces = ApiClient.get("/api/spaces", new TypeToken<List<Space>>(){}.getType());
        
        spacesData.clear();
        // spacesData.addAll(spaces);
        
        System.out.println("✅ Espacios cargados");
    }
    
    /**
     * Maneja el botón de búsqueda
     */
    @FXML
    private void handleSearch() {
        String query = searchField != null ? searchField.getText() : "";
        System.out.println("🔍 Buscando: " + query);
        
        // TODO: Filtrar espacios
        loadSpacesWithFilters();
    }
    
    /**
     * Maneja cambios en filtros
     */
    @FXML
    private void handleFiltersChanged() {
        System.out.println("🔄 Filtros cambiados");
        loadSpacesWithFilters();
    }
    
    /**
     * Carga espacios con filtros aplicados
     */
    private void loadSpacesWithFilters() {
        String search = searchField != null ? searchField.getText() : "";
        String status = statusFilter != null ? statusFilter.getValue() : "";
        String type = typeFilter != null ? typeFilter.getValue() : "";
        
        System.out.println("Filtros: search=" + search + ", status=" + status + ", type=" + type);
        
        // TODO: Aplicar filtros a la query de API
        loadSpaces();
    }
    
    /**
     * Maneja el botón "Crear Espacio"
     */
    @FXML
    private void handleCreate() {
        System.out.println("➕ Abriendo diálogo para crear espacio");
        
        // TODO: Abrir diálogo de creación
        showSpaceDialog(null);
    }
    
    /**
     * Maneja el botón "Refrescar"
     */
    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refrescando datos");
        loadSpaces();
    }
    
    /**
     * Edita un espacio
     */
    private void editSpace(Long spaceId) {
        System.out.println("✏️ Editando espacio ID: " + spaceId);
        
        // TODO: Cargar datos del espacio
        // Space space = ApiClient.get("/api/spaces/" + spaceId, Space.class);
        
        // Abrir diálogo con datos
        showSpaceDialog(spaceId);
    }
    
    /**
     * Elimina un espacio
     */
    private void deleteSpace(Long spaceId) {
        System.out.println("🗑️ Eliminando espacio ID: " + spaceId);
        
        // Confirmar
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Eliminación");
        confirmAlert.setHeaderText("¿Eliminar este espacio?");
        confirmAlert.setContentText("Esta acción no se puede deshacer.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Llamar a API
                // DELETE /api/spaces/{id}
                
                showAlert("Éxito", "Espacio eliminado exitosamente", Alert.AlertType.INFORMATION);
                loadSpaces();
            }
        });
    }
    
    /**
     * Muestra el diálogo de crear/editar espacio
     */
    private void showSpaceDialog(Long spaceId) {
        System.out.println("📝 Mostrando diálogo de espacio");
        
        // TODO: Crear y mostrar diálogo con formulario
        // Dialog<Space> dialog = new Dialog<>();
        // ... configurar formulario ...
        
        showAlert("Formulario", "Formulario de espacio - En desarrollo", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Muestra un diálogo de alerta
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
