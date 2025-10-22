package cr.una.reservas.frontend.ui.admin;

import cr.una.reservas.frontend.domain.JwtResponse;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista de Administración (solo para rol ADMIN)
 */
public class AdminView {
    
    private final VBox root;
    private final JwtResponse currentUser;
    
    private TabPane adminTabPane;
    
    public AdminView(JwtResponse currentUser) {
        this.currentUser = currentUser;
        this.root = new VBox(10);
        initializeUI();
    }
    
    private void initializeUI() {
        root.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Panel de Administración");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        adminTabPane = new TabPane();
        adminTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(adminTabPane, Priority.ALWAYS);
        
        // Tab de Gestión de Espacios
        Tab spacesTab = new Tab("Gestión de Espacios");
        spacesTab.setContent(createSpacesManagementView());
        
        // Tab de Gestión de Reservas
        Tab reservationsTab = new Tab("Gestión de Reservas");
        reservationsTab.setContent(createReservationsManagementView());
        
        // Tab de Gestión de Usuarios
        Tab usersTab = new Tab("Gestión de Usuarios");
        usersTab.setContent(createUsersManagementView());
        
        adminTabPane.getTabs().addAll(spacesTab, reservationsTab, usersTab);
        
        root.getChildren().addAll(titleLabel, new Separator(), adminTabPane);
    }
    
    private Parent createSpacesManagementView() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label label = new Label("Gestión de Espacios");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Button addButton = new Button("Agregar Nuevo Espacio");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        
        TableView<?> spacesTable = new TableView<>();
        spacesTable.setPlaceholder(new Label("No hay espacios registrados"));
        VBox.setVgrow(spacesTable, Priority.ALWAYS);
        
        content.getChildren().addAll(label, addButton, spacesTable);
        return content;
    }
    
    private Parent createReservationsManagementView() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label label = new Label("Gestión de Reservas");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox filterBox = new HBox(10);
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todas", "PENDIENTE", "CONFIRMADA", "CANCELADA");
        statusFilter.setValue("Todas");
        filterBox.getChildren().addAll(new Label("Estado:"), statusFilter);
        
        TableView<?> reservationsTable = new TableView<>();
        reservationsTable.setPlaceholder(new Label("No hay reservas"));
        VBox.setVgrow(reservationsTable, Priority.ALWAYS);
        
        content.getChildren().addAll(label, filterBox, reservationsTable);
        return content;
    }
    
    private Parent createUsersManagementView() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label label = new Label("Gestión de Usuarios");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TableView<?> usersTable = new TableView<>();
        usersTable.setPlaceholder(new Label("No hay usuarios registrados"));
        VBox.setVgrow(usersTable, Priority.ALWAYS);
        
        content.getChildren().addAll(label, usersTable);
        return content;
    }
    
    public Parent getView() {
        return root;
    }
}
