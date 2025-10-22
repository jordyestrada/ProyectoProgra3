package cr.una.reservas.frontend.controllers;

import cr.una.reservas.frontend.util.FlowController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Controlador Unificado de Vistas Principales
 * 
 * Maneja 3 vistas en un solo FXML:
 * - Dashboard (vboxDashboard)
 * - Catálogo de Espacios (vboxSpacesCatalog)
 * - Mis Reservas (vboxMyBookings)
 * 
 * @author Sistema de Reservas - Municipalidad de Pérez Zeledón
 */
public class MainController {
    
    // ============================================
    // Componentes Compartidos (Navbar/Sidebar)
    // ============================================
    @FXML private ImageView navbarLogo;
    @FXML private Button notificationsButton;
    @FXML private MenuButton userMenuButton;
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardNavButton;
    @FXML private Button espaciosNavButton;
    @FXML private Button reservasNavButton;
    @FXML private VBox adminSection;
    
    // ============================================
    // Contenedores de Vistas
    // ============================================
    @FXML private VBox vboxDashboard;
    @FXML private VBox vboxSpacesCatalog;
    @FXML private VBox vboxMyBookings;
    
    // ============================================
    // Dashboard Components
    // ============================================
    @FXML private Label activeBookingsCount;
    @FXML private Label nextBookingDate;
    @FXML private Label availableSpacesCount;
    @FXML private Label totalBookingsCount;
    @FXML private VBox upcomingBookingsList;
    @FXML private GridPane featuredSpacesGrid;
    
    // ============================================
    // Spaces Catalog Components
    // ============================================
    @FXML private Button newSpaceButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> tipoComboBox;
    @FXML private ComboBox<String> capacidadComboBox;
    @FXML private GridPane spacesGrid;
    @FXML private VBox emptyStateSpaces;
    
    // ============================================
    // My Bookings Components
    // ============================================
    @FXML private ToggleGroup filterGroup;
    @FXML private ToggleButton allFilter;
    @FXML private ToggleButton activeFilter;
    @FXML private ToggleButton completedFilter;
    @FXML private ToggleButton cancelledFilter;
    @FXML private VBox bookingsList;
    @FXML private VBox emptyStateBookings;
    
    // ============================================
    // Estado Actual
    // ============================================
    private String currentView = "dashboard";
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        System.out.println("✓ MainController inicializado");
        
        // Mostrar Dashboard por defecto
        showDashboard();
        
        // Cargar datos iniciales
        loadUserInfo();
        loadDashboardData();
        loadSpacesData();
        loadBookingsData();
    }
    
    // ============================================
    // NAVEGACIÓN ENTRE VISTAS
    // ============================================
    
    /**
     * Muestra la vista Dashboard
     */
    @FXML
    public void showDashboard() {
        System.out.println("🏠 Mostrando Dashboard");
        currentView = "dashboard";
        
        // Ocultar todas las vistas
        vboxDashboard.setVisible(false);
        vboxSpacesCatalog.setVisible(false);
        vboxMyBookings.setVisible(false);
        
        // Mostrar Dashboard
        vboxDashboard.setVisible(true);
        
        // Actualizar botones activos
        updateActiveButton(dashboardNavButton);
        
        // Recargar datos
        loadDashboardData();
    }
    
    /**
     * Muestra la vista de Catálogo de Espacios
     */
    @FXML
    public void showSpacesCatalog() {
        System.out.println("🏛️ Mostrando Catálogo de Espacios");
        currentView = "spaces-catalog";
        
        // Ocultar todas las vistas
        vboxDashboard.setVisible(false);
        vboxSpacesCatalog.setVisible(false);
        vboxMyBookings.setVisible(false);
        
        // Mostrar Catálogo
        vboxSpacesCatalog.setVisible(true);
        
        // Actualizar botones activos
        updateActiveButton(espaciosNavButton);
        
        // Recargar datos
        loadSpacesData();
    }
    
    /**
     * Muestra la vista de Mis Reservas
     */
    @FXML
    public void showMyBookings() {
        System.out.println("📅 Mostrando Mis Reservas");
        currentView = "my-bookings";
        
        // Ocultar todas las vistas
        vboxDashboard.setVisible(false);
        vboxSpacesCatalog.setVisible(false);
        vboxMyBookings.setVisible(false);
        
        // Mostrar Mis Reservas
        vboxMyBookings.setVisible(true);
        
        // Actualizar botones activos
        updateActiveButton(reservasNavButton);
        
        // Recargar datos
        loadBookingsData();
    }
    
    /**
     * Actualiza el botón activo en el sidebar
     */
    private void updateActiveButton(Button activeButton) {
        // Remover clase activa de todos
        dashboardNavButton.getStyleClass().remove("sidebar-button-active");
        espaciosNavButton.getStyleClass().remove("sidebar-button-active");
        reservasNavButton.getStyleClass().remove("sidebar-button-active");
        
        // Agregar clase activa al botón seleccionado
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
    
    // ============================================
    // NAVEGACIÓN A VISTAS ADMIN (desde sidebar)
    // ============================================
    
    @FXML
    public void showAdminSpaces() {
        System.out.println("🔧 Navegando a Admin Espacios");
        FlowController.getInstance().goView("admin"); // admin.fxml
    }
    
    @FXML
    public void showAdminReports() {
        System.out.println("📊 Navegando a Admin Reportes");
        FlowController.getInstance().goView("admin"); // admin.fxml mostrará reportes
    }
    
    // ============================================
    // ACCIONES NAVBAR
    // ============================================
    
    @FXML
    public void handleProfile() {
        System.out.println("👤 Abriendo perfil de usuario");
        showAlert("Mi Perfil", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleSettings() {
        System.out.println("⚙️ Abriendo configuración");
        showAlert("Configuración", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleLogout() {
        System.out.println("🚪 Cerrando sesión");
        FlowController.getInstance().goView("login");
    }
    
    // ============================================
    // ACCIONES DASHBOARD
    // ============================================
    
    @FXML
    public void handleNewBooking() {
        System.out.println("➕ Nueva reserva desde Dashboard");
        // Primero mostrar catálogo para seleccionar espacio
        showSpacesCatalog();
    }
    
    // ============================================
    // ACCIONES SPACES CATALOG
    // ============================================
    
    @FXML
    public void handleSearch() {
        System.out.println("🔍 Buscando: " + searchField.getText());
        applyFilters();
    }
    
    @FXML
    public void handleFilter() {
        System.out.println("🔍 Aplicando filtros desde ComboBox");
        applyFilters();
    }
    
    @FXML
    public void handleClearFilters() {
        System.out.println("🧹 Limpiando filtros");
        searchField.clear();
        tipoComboBox.setValue(null);
        capacidadComboBox.setValue(null);
        loadSpacesData();
    }
    
    @FXML
    public void handleNuevoEspacio() {
        System.out.println("➕ Nuevo espacio (solo admin)");
        showAlert("Nuevo Espacio", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    private void applyFilters() {
        System.out.println("Aplicando filtros...");
        // TODO: Implementar lógica de filtrado real
        loadSpacesData();
    }
    
    // ============================================
    // ACCIONES MY BOOKINGS
    // ============================================
    
    @FXML
    public void handleFilterChange() {
        if (filterGroup == null || filterGroup.getSelectedToggle() == null) {
            return;
        }
        
        Toggle selected = filterGroup.getSelectedToggle();
        System.out.println("🔄 Filtro cambiado: " + ((ToggleButton) selected).getText());
        
        if (selected == allFilter) {
            handleAllFilter();
        } else if (selected == activeFilter) {
            handleActiveFilter();
        } else if (selected == completedFilter) {
            handleCompletedFilter();
        } else if (selected == cancelledFilter) {
            handleCancelledFilter();
        }
    }
    
    private void handleAllFilter() {
        System.out.println("📋 Mostrando todas las reservas");
        loadBookingsData();
    }
    
    private void handleActiveFilter() {
        System.out.println("✅ Mostrando reservas activas");
        // TODO: Filtrar solo activas
        loadBookingsData();
    }
    
    private void handleCompletedFilter() {
        System.out.println("✔️ Mostrando reservas completadas");
        // TODO: Filtrar solo completadas
        loadBookingsData();
    }
    
    private void handleCancelledFilter() {
        System.out.println("❌ Mostrando reservas canceladas");
        // TODO: Filtrar solo canceladas
        loadBookingsData();
    }
    
    @FXML
    public void handleExportar() {
        System.out.println("📥 Exportando reservas");
        showAlert("Exportar", "Funcionalidad de exportación en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleVerEspacios() {
        System.out.println("🏛️ Ver Espacios desde empty state");
        showSpacesCatalog();
    }
    
    // ============================================
    // CARGA DE DATOS (Mock)
    // ============================================
    
    private void loadUserInfo() {
        // TODO: Cargar datos reales del usuario desde API
        userNameLabel.setText("Usuario Demo");
        userRoleLabel.setText("Ciudadano");
    }
    
    private void loadDashboardData() {
        System.out.println("📊 Cargando datos del Dashboard");
        // TODO: Cargar datos reales desde API
        activeBookingsCount.setText("5");
        nextBookingDate.setText("24 Oct");
        availableSpacesCount.setText("12");
        totalBookingsCount.setText("23");
    }
    
    private void loadSpacesData() {
        System.out.println("🏛️ Cargando espacios");
        // TODO: Cargar datos reales desde API
        // Por ahora, mostrar empty state o datos mock
    }
    
    private void loadBookingsData() {
        System.out.println("📅 Cargando reservas");
        // TODO: Cargar datos reales desde API
        // Por ahora, mostrar empty state o datos mock
    }
    
    // ============================================
    // UTILIDADES
    // ============================================
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
