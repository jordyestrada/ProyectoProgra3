package cr.una.reservas.frontend.ui.reports;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Controller para la pantalla de reportes administrativos
 * Muestra KPIs, gráficos y tabla de últimas reservas
 */
public class AdminReportsController {
    
    // KPI Cards
    @FXML private Label totalReservationsKPI;
    @FXML private Label revenueKPI;
    @FXML private Label activeSpacesKPI;
    @FXML private Label occupancyRateKPI;
    
    // Charts
    @FXML private BarChart<String, Number> monthlyReservationsChart;
    @FXML private PieChart popularSpacesChart;
    
    // Recent reservations table
    @FXML private TableView<Object> recentReservationsTable;
    @FXML private TableColumn<Object, Long> idColumn;
    @FXML private TableColumn<Object, String> userColumn;
    @FXML private TableColumn<Object, String> spaceColumn;
    @FXML private TableColumn<Object, String> dateColumn;
    @FXML private TableColumn<Object, String> statusColumn;
    
    // Date range selector
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button applyDateRangeButton;
    
    // Actions
    @FXML private Button refreshButton;
    @FXML private Button exportExcelButton;
    @FXML private Button exportPdfButton;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ AdminReportsController inicializado");
        
        // Cargar KPIs
        loadKPIs();
        
        // Cargar gráficos
        loadCharts();
        
        // Configurar tabla
        setupRecentReservationsTable();
        
        // Cargar reservas recientes
        loadRecentReservations();
    }
    
    /**
     * Carga los KPIs desde la API
     */
    private void loadKPIs() {
        System.out.println("📊 Cargando KPIs...");
        
        // TODO: Llamar a GET /api/reports/stats
        
        // Mock data
        if (totalReservationsKPI != null) {
            totalReservationsKPI.setText("248");
        }
        if (revenueKPI != null) {
            revenueKPI.setText("₡12.5M");
        }
        if (activeSpacesKPI != null) {
            activeSpacesKPI.setText("15");
        }
        if (occupancyRateKPI != null) {
            occupancyRateKPI.setText("78%");
        }
        
        System.out.println("✅ KPIs cargados");
    }
    
    /**
     * Carga los datos de los gráficos
     */
    private void loadCharts() {
        loadMonthlyReservationsChart();
        loadPopularSpacesChart();
    }
    
    /**
     * Carga el gráfico de barras de reservas mensuales
     */
    private void loadMonthlyReservationsChart() {
        System.out.println("📊 Cargando gráfico de reservas mensuales");
        
        if (monthlyReservationsChart != null) {
            // TODO: Llamar a GET /api/reports/monthly
            
            // Mock data
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Reservas");
            
            series.getData().add(new XYChart.Data<>("Ene", 25));
            series.getData().add(new XYChart.Data<>("Feb", 30));
            series.getData().add(new XYChart.Data<>("Mar", 35));
            series.getData().add(new XYChart.Data<>("Abr", 28));
            series.getData().add(new XYChart.Data<>("May", 40));
            series.getData().add(new XYChart.Data<>("Jun", 38));
            series.getData().add(new XYChart.Data<>("Jul", 45));
            series.getData().add(new XYChart.Data<>("Ago", 42));
            series.getData().add(new XYChart.Data<>("Sep", 38));
            series.getData().add(new XYChart.Data<>("Oct", 35));
            
            monthlyReservationsChart.getData().clear();
            monthlyReservationsChart.getData().add(series);
        }
    }
    
    /**
     * Carga el gráfico circular de espacios populares
     */
    private void loadPopularSpacesChart() {
        System.out.println("📊 Cargando gráfico de espacios populares");
        
        if (popularSpacesChart != null) {
            // TODO: Llamar a GET /api/reports/popular-spaces
            
            // Mock data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Salón Principal", 35),
                new PieChart.Data("Auditorio", 28),
                new PieChart.Data("Cancha", 20),
                new PieChart.Data("Sala Reuniones", 12),
                new PieChart.Data("Otros", 5)
            );
            
            popularSpacesChart.setData(pieChartData);
        }
    }
    
    /**
     * Configura la tabla de reservas recientes
     */
    private void setupRecentReservationsTable() {
        if (recentReservationsTable != null) {
            // TODO: Configurar columnas de la tabla
            System.out.println("⚙️ Configurando tabla de reservas recientes");
        }
    }
    
    /**
     * Carga las reservas recientes
     */
    private void loadRecentReservations() {
        System.out.println("📅 Cargando reservas recientes");
        
        // TODO: Llamar a GET /api/reservations?limit=10&sort=createdAt,desc
    }
    
    /**
     * Maneja el botón de aplicar rango de fechas
     */
    @FXML
    private void handleApplyDateRange() {
        System.out.println("📅 Aplicando rango de fechas");
        
        // Recargar datos con el rango de fechas
        loadKPIs();
        loadCharts();
        loadRecentReservations();
    }
    
    /**
     * Maneja el botón de refrescar
     */
    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refrescando datos");
        
        loadKPIs();
        loadCharts();
        loadRecentReservations();
    }
    
    /**
     * Maneja el botón de exportar a Excel
     */
    @FXML
    private void handleExportExcel() {
        System.out.println("📊 Exportando a Excel");
        
        // TODO: Generar archivo Excel
        showAlert("Exportar", "Exportación a Excel - En desarrollo", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Maneja el botón de exportar a PDF
     */
    @FXML
    private void handleExportPdf() {
        System.out.println("📄 Exportando a PDF");
        
        // TODO: Generar archivo PDF
        showAlert("Exportar", "Exportación a PDF - En desarrollo", Alert.AlertType.INFORMATION);
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
