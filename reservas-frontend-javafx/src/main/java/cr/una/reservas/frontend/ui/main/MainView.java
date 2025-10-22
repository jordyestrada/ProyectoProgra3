package cr.una.reservas.frontend.ui.main;

import cr.una.reservas.frontend.domain.JwtResponse;
import cr.una.reservas.frontend.ui.spaces.SpacesView;
import cr.una.reservas.frontend.ui.reservations.ReservationsView;
import cr.una.reservas.frontend.ui.admin.AdminView;
import cr.una.reservas.frontend.ui.reports.ReportsView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista principal del sistema (después del login)
 * Muestra diferentes secciones según el rol del usuario
 */
public class MainView {
    
    private final BorderPane root;
    private final JwtResponse currentUser;
    
    private TabPane tabPane;
    
    public MainView(JwtResponse currentUser) {
        this.currentUser = currentUser;
        this.root = new BorderPane();
        initializeUI();
    }
    
    private void initializeUI() {
        // Header
        HBox header = createHeader();
        root.setTop(header);
        
        // Content con tabs
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab de Espacios (todos los usuarios)
        Tab spacesTab = new Tab("Espacios");
        SpacesView spacesView = new SpacesView(currentUser);
        spacesTab.setContent(spacesView.getView());
        tabPane.getTabs().add(spacesTab);
        
        // Tab de Mis Reservas (todos los usuarios)
        Tab reservationsTab = new Tab("Mis Reservas");
        ReservationsView reservationsView = new ReservationsView(currentUser);
        reservationsTab.setContent(reservationsView.getView());
        tabPane.getTabs().add(reservationsTab);
        
        // Tab de Administración (solo ADMIN)
        if (isAdmin()) {
            Tab adminTab = new Tab("Administración");
            AdminView adminView = new AdminView(currentUser);
            adminTab.setContent(adminView.getView());
            tabPane.getTabs().add(adminTab);
            
            // Tab de Reportes (solo ADMIN)
            Tab reportsTab = new Tab("Reportes");
            ReportsView reportsView = new ReportsView(currentUser);
            reportsTab.setContent(reportsView.getView());
            tabPane.getTabs().add(reportsTab);
        }
        
        root.setCenter(tabPane);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        Label titleLabel = new Label("Sistema de Reservas Municipales");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userLabel = new Label("Usuario: " + currentUser.getEmail());
        userLabel.setStyle("-fx-text-fill: white;");
        
        Label roleLabel = new Label("Rol: " + (isAdmin() ? "Administrador" : "Usuario"));
        roleLabel.setStyle("-fx-text-fill: white;");
        
        Button logoutButton = new Button("Cerrar Sesión");
        logoutButton.setOnAction(e -> logout());
        
        header.getChildren().addAll(titleLabel, spacer, userLabel, roleLabel, logoutButton);
        
        return header;
    }
    
    private boolean isAdmin() {
        return currentUser.getRoles() != null && 
               currentUser.getRoles().stream().anyMatch(r -> r.equals("ROLE_ADMIN") || r.equals("ADMIN"));
    }
    
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Está seguro que desea cerrar sesión?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Implementar logout y volver a login
                System.exit(0);
            }
        });
    }
    
    public Parent getView() {
        return root;
    }
}
