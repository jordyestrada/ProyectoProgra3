package cr.una.reservas.frontend.controllers;

import cr.una.reservas.frontend.util.FlowController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controlador Unificado de Vistas Administrativas
 * 
 * Maneja 2 vistas en un solo FXML:
 * - Gestión de Espacios (vboxAdminSpaces)
 * - Reportes (vboxAdminReports)
 * 
 * @author Sistema de Reservas - Municipalidad de Pérez Zeledón
 */
public class AdminController {
    
    // ============================================
    // Componentes Compartidos
    // ============================================
    @FXML private ImageView navbarLogo;
    @FXML private Button notificationsButton;
    @FXML private MenuButton userMenuButton;
    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private Button adminSpacesNavButton;
    @FXML private Button adminReportsNavButton;
    
    // ============================================
    // Contenedores de Vistas
    // ============================================
    @FXML private VBox vboxAdminSpaces;
    @FXML private VBox vboxAdminReports;
    
    // ============================================
    // Admin Spaces Components
    // ============================================
    @FXML private Label totalSpacesCount;
    @FXML private Label activeSpacesCount;
    @FXML private Label maintenanceSpacesCount;
    @FXML private Label occupancyRate;
    @FXML private TableView<?> spacesTable;
    @FXML private TableColumn<?, ?> idColumn;
    @FXML private TableColumn<?, ?> nameColumn;
    @FXML private TableColumn<?, ?> typeColumn;
    @FXML private TableColumn<?, ?> capacityColumn;
    @FXML private TableColumn<?, ?> statusColumn;
    @FXML private TableColumn<?, ?> locationColumn;
    @FXML private TableColumn<?, ?> actionsColumn;
    
    // ============================================
    // Admin Reports Components
    // ============================================
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private Label totalBookingsReport;
    @FXML private Label uniqueUsersReport;
    @FXML private Label revenueReport;
    @FXML private Label avgRatingReport;
    @FXML private VBox monthlyChartContainer;
    @FXML private VBox topSpacesChartContainer;
    @FXML private TableView<?> reportsTable;
    @FXML private TableColumn<?, ?> reportDateColumn;
    @FXML private TableColumn<?, ?> reportSpaceColumn;
    @FXML private TableColumn<?, ?> reportUserColumn;
    @FXML private TableColumn<?, ?> reportDurationColumn;
    @FXML private TableColumn<?, ?> reportStatusColumn;
    @FXML private TableColumn<?, ?> reportRevenueColumn;
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        System.out.println("✓ AdminController inicializado");
        
        // Mostrar Gestión de Espacios por defecto
        showAdminSpaces();
        
        // Cargar datos iniciales
        loadUserInfo();
        loadSpacesData();
    }
    
    // ============================================
    // NAVEGACIÓN ENTRE VISTAS
    // ============================================
    
    @FXML
    public void showAdminSpaces() {
        System.out.println("🔧 Mostrando Gestión de Espacios");
        
        // Ocultar todas las vistas
        vboxAdminSpaces.setVisible(false);
        vboxAdminReports.setVisible(false);
        
        // Mostrar Gestión de Espacios
        vboxAdminSpaces.setVisible(true);
        
        // Actualizar botones activos
        updateActiveButton(adminSpacesNavButton);
        
        // Recargar datos
        loadSpacesData();
    }
    
    @FXML
    public void showAdminReports() {
        System.out.println("📊 Mostrando Reportes");
        
        // Ocultar todas las vistas
        vboxAdminSpaces.setVisible(false);
        vboxAdminReports.setVisible(false);
        
        // Mostrar Reportes
        vboxAdminReports.setVisible(true);
        
        // Actualizar botones activos
        updateActiveButton(adminReportsNavButton);
        
        // Recargar datos
        loadReportsData();
    }
    
    private void updateActiveButton(Button activeButton) {
        // Remover clase activa de todos
        adminSpacesNavButton.getStyleClass().remove("sidebar-button-active");
        adminReportsNavButton.getStyleClass().remove("sidebar-button-active");
        
        // Agregar clase activa al botón seleccionado
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
    
    // ============================================
    // NAVEGACIÓN NAVBAR
    // ============================================
    
    @FXML
    public void handleDashboard() {
        System.out.println("🏠 Navegando al Dashboard");
        FlowController.getInstance().goView("main");
    }
    
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
    // ACCIONES ADMIN SPACES
    // ============================================
    
    @FXML
    public void handleNewSpace() {
        System.out.println("➕ Creando nuevo espacio");
        showAlert("Nuevo Espacio", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleExport() {
        System.out.println("📥 Exportando espacios");
        showAlert("Exportar", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    // ============================================
    // ACCIONES ADMIN REPORTS
    // ============================================
    
    @FXML
    public void handleGenerateReport() {
        System.out.println("📊 Generando reporte");
        showAlert("Generar Reporte", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    @FXML
    public void handleExportReport() {
        System.out.println("📥 Exportando reporte");
        showAlert("Exportar Reporte", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }
    
    // ============================================
    // CARGA DE DATOS
    // ============================================
    
    private void loadUserInfo() {
        userNameLabel.setText("Admin Demo");
    }
    
    private void loadSpacesData() {
        System.out.println("🏛️ Cargando datos de espacios");
        // TODO: Cargar datos reales desde API
        totalSpacesCount.setText("15");
        activeSpacesCount.setText("12");
        maintenanceSpacesCount.setText("2");
        occupancyRate.setText("78%");
    }
    
    private void loadReportsData() {
        System.out.println("📊 Cargando datos de reportes");
        // TODO: Cargar datos reales desde API
        totalBookingsReport.setText("145");
        uniqueUsersReport.setText("87");
        revenueReport.setText("₡245K");
        avgRatingReport.setText("4.5");
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
