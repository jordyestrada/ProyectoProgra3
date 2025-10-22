package cr.una.reservas.frontend.ui.main;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * Controller para el Dashboard principal
 * Muestra estadísticas y navegación principal del sistema
 */
public class DashboardController {
    
    // Navbar elements
    @FXML private Label navbarTitle;
    @FXML private Button notificationsButton;
    @FXML private MenuButton profileMenu;
    
    // Sidebar elements
    @FXML private Label usernameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardMenuItem;
    @FXML private Button spacesMenuItem;
    @FXML private Button bookingsMenuItem;
    @FXML private Button adminSpacesMenuItem;
    @FXML private Button adminReportsMenuItem;
    @FXML private Button logoutMenuItem;
    
    // Stats cards
    @FXML private Label totalReservationsValue;
    @FXML private Label activeReservationsValue;
    @FXML private Label completedReservationsValue;
    @FXML private Label availableSpacesValue;
    
    // Content area
    @FXML private VBox upcomingReservationsContainer;
    @FXML private VBox quickActionsContainer;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ DashboardController inicializado");
        
        // Cargar datos del usuario
        loadUserData();
        
        // Cargar estadísticas
        loadStatistics();
        
        // Cargar próximas reservas
        loadUpcomingReservations();
        
        // Configurar menú activo
        setActiveMenuItem(dashboardMenuItem);
    }
    
    /**
     * Carga datos del usuario actual
     */
    private void loadUserData() {
        // TODO: Obtener del SessionManager cuando esté implementado
        if (usernameLabel != null) {
            usernameLabel.setText("Usuario Demo");
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText("Cliente");
        }
    }
    
    /**
     * Carga las estadísticas del dashboard
     */
    private void loadStatistics() {
        // TODO: Llamar a la API para obtener estadísticas reales
        if (totalReservationsValue != null) {
            totalReservationsValue.setText("24");
        }
        if (activeReservationsValue != null) {
            activeReservationsValue.setText("3");
        }
        if (completedReservationsValue != null) {
            completedReservationsValue.setText("18");
        }
        if (availableSpacesValue != null) {
            availableSpacesValue.setText("12");
        }
    }
    
    /**
     * Carga las próximas reservas del usuario
     */
    private void loadUpcomingReservations() {
        // TODO: Llamar a GET /api/reservations/user/{id}?status=ACTIVE
        System.out.println("📅 Cargando próximas reservas...");
    }
    
    /**
     * Navega a la pantalla de Espacios
     */
    @FXML
    private void handleSpacesMenuItem() {
        System.out.println("📍 Navegando a Espacios");
        navigateTo("/fxml/spaces-catalog.fxml", "Catálogo de Espacios");
        setActiveMenuItem(spacesMenuItem);
    }
    
    /**
     * Navega a Mis Reservas
     */
    @FXML
    private void handleBookingsMenuItem() {
        System.out.println("📅 Navegando a Mis Reservas");
        navigateTo("/fxml/my-bookings.fxml", "Mis Reservas");
        setActiveMenuItem(bookingsMenuItem);
    }
    
    /**
     * Navega a Admin - Espacios
     */
    @FXML
    private void handleAdminSpacesMenuItem() {
        System.out.println("⚙️ Navegando a Admin - Espacios");
        navigateTo("/fxml/admin-spaces.fxml", "Administración de Espacios");
        setActiveMenuItem(adminSpacesMenuItem);
    }
    
    /**
     * Navega a Admin - Reportes
     */
    @FXML
    private void handleAdminReportsMenuItem() {
        System.out.println("📊 Navegando a Admin - Reportes");
        navigateTo("/fxml/admin-reports.fxml", "Reportes Administrativos");
        setActiveMenuItem(adminReportsMenuItem);
    }
    
    /**
     * Maneja el botón de notificaciones
     */
    @FXML
    private void handleNotifications() {
        System.out.println("🔔 Abriendo notificaciones");
        showAlert("Notificaciones", "No tienes notificaciones nuevas", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Maneja el logout
     */
    @FXML
    private void handleLogout() {
        System.out.println("👋 Cerrando sesión");
        // TODO: Limpiar SessionManager
        navigateToLogin();
    }
    
    /**
     * Navega a otra pantalla
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) dashboardMenuItem.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + title);
            System.out.println("✅ Navegando a: " + title);
        } catch (Exception e) {
            System.err.println("❌ Error al navegar a " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "No se pudo cargar la pantalla: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Navega al login
     */
    private void navigateToLogin() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) logoutMenuItem.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1024, 768);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - Login");
            System.out.println("✅ Navegando al Login");
        } catch (Exception e) {
            System.err.println("❌ Error al navegar al login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Marca un item del menú como activo
     */
    private void setActiveMenuItem(Button menuItem) {
        // Remover clase activa de todos
        if (dashboardMenuItem != null) dashboardMenuItem.getStyleClass().remove("sidebar-item-active");
        if (spacesMenuItem != null) spacesMenuItem.getStyleClass().remove("sidebar-item-active");
        if (bookingsMenuItem != null) bookingsMenuItem.getStyleClass().remove("sidebar-item-active");
        if (adminSpacesMenuItem != null) adminSpacesMenuItem.getStyleClass().remove("sidebar-item-active");
        if (adminReportsMenuItem != null) adminReportsMenuItem.getStyleClass().remove("sidebar-item-active");
        
        // Agregar clase activa al item actual
        if (menuItem != null && !menuItem.getStyleClass().contains("sidebar-item-active")) {
            menuItem.getStyleClass().add("sidebar-item-active");
        }
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
