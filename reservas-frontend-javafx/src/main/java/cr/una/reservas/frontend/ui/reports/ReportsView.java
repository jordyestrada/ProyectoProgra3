package cr.una.reservas.frontend.ui.reports;

import cr.una.reservas.frontend.domain.JwtResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista de Reportes (solo para rol ADMIN)
 */
public class ReportsView {
    
    private final VBox root;
    private final JwtResponse currentUser;
    
    public ReportsView(JwtResponse currentUser) {
        this.currentUser = currentUser;
        this.root = new VBox(15);
        initializeUI();
    }
    
    private void initializeUI() {
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Reportes del Sistema");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Sección de filtros
        VBox filterSection = createFilterSection();
        
        // Sección de reportes disponibles
        GridPane reportsGrid = createReportsGrid();
        
        root.getChildren().addAll(titleLabel, new Separator(), filterSection, new Separator(), reportsGrid);
    }
    
    private VBox createFilterSection() {
        VBox section = new VBox(10);
        
        Label label = new Label("Filtros");
        label.setStyle("-fx-font-weight: bold;");
        
        HBox dateBox = new HBox(10);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        
        Label fromLabel = new Label("Desde:");
        DatePicker fromDate = new DatePicker();
        
        Label toLabel = new Label("Hasta:");
        DatePicker toDate = new DatePicker();
        
        Button applyButton = new Button("Aplicar");
        applyButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        dateBox.getChildren().addAll(fromLabel, fromDate, toLabel, toDate, applyButton);
        
        section.getChildren().addAll(label, dateBox);
        return section;
    }
    
    private GridPane createReportsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        
        // Reporte 1: Reservas por Estado
        Button report1 = createReportButton(
            "Reservas por Estado",
            "Cantidad de reservas agrupadas por estado"
        );
        
        // Reporte 2: Espacios más Reservados
        Button report2 = createReportButton(
            "Espacios Más Reservados",
            "Top 10 de espacios con más reservas"
        );
        
        // Reporte 3: Ingresos por Período
        Button report3 = createReportButton(
            "Ingresos por Período",
            "Total de ingresos en el período seleccionado"
        );
        
        // Reporte 4: Ocupación de Espacios
        Button report4 = createReportButton(
            "Ocupación de Espacios",
            "Porcentaje de ocupación por espacio"
        );
        
        grid.add(report1, 0, 0);
        grid.add(report2, 1, 0);
        grid.add(report3, 0, 1);
        grid.add(report4, 1, 1);
        
        return grid;
    }
    
    private Button createReportButton(String title, String description) {
        Button button = new Button();
        
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setPrefSize(250, 150);
        content.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 11px;");
        descLabel.setWrapText(true);
        
        content.getChildren().addAll(titleLabel, descLabel);
        
        button.setGraphic(content);
        button.setStyle("-fx-background-color: white;");
        button.setOnAction(e -> generateReport(title));
        
        return button;
    }
    
    private void generateReport(String reportName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Generar Reporte");
        alert.setHeaderText("Generando: " + reportName);
        alert.setContentText("Funcionalidad en desarrollo...");
        alert.showAndWait();
    }
    
    public Parent getView() {
        return root;
    }
}
