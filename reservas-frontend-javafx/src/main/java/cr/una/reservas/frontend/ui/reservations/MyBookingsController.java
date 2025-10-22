package cr.una.reservas.frontend.ui.reservations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller para la pantalla "Mis Reservas"
 * Muestra las reservas del usuario actual con filtros por estado
 */
public class MyBookingsController {
    
    // Filters
    @FXML private ToggleButton allFilter;
    @FXML private ToggleButton activeFilter;
    @FXML private ToggleButton completedFilter;
    @FXML private ToggleButton cancelledFilter;
    @FXML private ToggleGroup filterGroup;
    
    // Content
    @FXML private VBox bookingsContainer;
    @FXML private ScrollPane scrollPane;
    
    // Empty state
    @FXML private VBox emptyStateContainer;
    
    // Navigation
    @FXML private Button newBookingButton;
    @FXML private Button dashboardButton;
    
    private String currentFilter = "ALL";
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ MyBookingsController inicializado");
        
        // Configurar filtros
        setupFilters();
        
        // Cargar reservas
        loadBookings("ALL");
    }
    
    /**
     * Configura los filtros de estado
     */
    private void setupFilters() {
        if (filterGroup == null) {
            filterGroup = new ToggleGroup();
        }
        
        if (allFilter != null) {
            allFilter.setToggleGroup(filterGroup);
            allFilter.setSelected(true);
        }
        if (activeFilter != null) {
            activeFilter.setToggleGroup(filterGroup);
        }
        if (completedFilter != null) {
            completedFilter.setToggleGroup(filterGroup);
        }
        if (cancelledFilter != null) {
            cancelledFilter.setToggleGroup(filterGroup);
        }
    }
    
    /**
     * Carga las reservas del usuario
     */
    private void loadBookings(String status) {
        System.out.println("📅 Cargando reservas con estado: " + status);
        
        // TODO: Llamar a API
        // GET /api/reservations/user/{userId}?status={status}
        
        // Limpiar contenedor
        if (bookingsContainer != null) {
            bookingsContainer.getChildren().clear();
        }
        
        // Mock - Por ahora mostrar empty state
        showEmptyState(true);
    }
    
    /**
     * Muestra/oculta el estado vacío
     */
    private void showEmptyState(boolean show) {
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisible(show);
        }
        if (bookingsContainer != null) {
            bookingsContainer.setVisible(!show);
        }
    }
    
    /**
     * Maneja filtro "Todas"
     */
    @FXML
    private void handleAllFilter() {
        currentFilter = "ALL";
        loadBookings("ALL");
    }
    
    /**
     * Maneja filtro "Activas"
     */
    @FXML
    private void handleActiveFilter() {
        currentFilter = "ACTIVE";
        loadBookings("ACTIVE");
    }
    
    /**
     * Maneja filtro "Completadas"
     */
    @FXML
    private void handleCompletedFilter() {
        currentFilter = "COMPLETED";
        loadBookings("COMPLETED");
    }
    
    /**
     * Maneja filtro "Canceladas"
     */
    @FXML
    private void handleCancelledFilter() {
        currentFilter = "CANCELLED";
        loadBookings("CANCELLED");
    }
    
    /**
     * Maneja el botón de nueva reserva
     */
    @FXML
    private void handleNewBooking() {
        System.out.println("➕ Navegando a catálogo para nueva reserva");
        navigateTo("/fxml/spaces-catalog.fxml", "Catálogo de Espacios");
    }
    
    /**
     * Maneja el botón de dashboard
     */
    @FXML
    private void handleDashboard() {
        System.out.println("🏠 Navegando al dashboard");
        navigateTo("/fxml/dashboard.fxml", "Dashboard");
    }
    
    /**
     * Muestra el código QR de una reserva
     */
    private void showQRCode(Long reservationId) {
        System.out.println("📱 Mostrando QR de reserva ID: " + reservationId);
        
        // TODO: Obtener QR code
        // GET /api/reservations/{id}/qr
        
        showAlert("QR Code", "QR Code - En desarrollo", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Cancela una reserva
     */
    private void cancelBooking(Long reservationId) {
        System.out.println("❌ Cancelando reserva ID: " + reservationId);
        
        // Confirmar
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar Cancelación");
        confirmAlert.setHeaderText("¿Estás seguro?");
        confirmAlert.setContentText("Esta acción no se puede deshacer.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Llamar a API
                // DELETE /api/reservations/{id}
                
                showAlert("Éxito", "Reserva cancelada exitosamente", Alert.AlertType.INFORMATION);
                loadBookings(currentFilter);
            }
        });
    }
    
    /**
     * Descarga los detalles de una reserva
     */
    private void downloadBooking(Long reservationId) {
        System.out.println("💾 Descargando reserva ID: " + reservationId);
        
        // TODO: Generar PDF o imagen
        showAlert("Descarga", "Descarga - En desarrollo", Alert.AlertType.INFORMATION);
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
    
    /**
     * Navega a otra pantalla
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) dashboardButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + title);
        } catch (Exception e) {
            System.err.println("❌ Error al navegar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
