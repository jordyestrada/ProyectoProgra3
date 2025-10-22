package cr.una.reservas.frontend.ui.spaces;

import cr.una.reservas.frontend.domain.JwtResponse;
import cr.una.reservas.frontend.domain.Space;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista de Espacios (listado y búsqueda de espacios disponibles)
 */
public class SpacesView {
    
    private final VBox root;
    private final JwtResponse currentUser;
    
    private TextField searchField;
    private ListView<Space> spacesListView;
    private Button searchButton;
    private Button refreshButton;
    
    public SpacesView(JwtResponse currentUser) {
        this.currentUser = currentUser;
        this.root = new VBox(10);
        initializeUI();
    }
    
    private void initializeUI() {
        root.setPadding(new Insets(20));
        
        // Header con búsqueda
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Buscar espacios:");
        searchField = new TextField();
        searchField.setPromptText("Nombre, tipo, ubicación...");
        searchField.setPrefWidth(300);
        
        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        
        refreshButton = new Button("Actualizar");
        refreshButton.setOnAction(e -> loadSpaces());
        
        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, refreshButton);
        
        // Lista de espacios
        spacesListView = new ListView<>();
        spacesListView.setCellFactory(param -> new SpaceListCell());
        spacesListView.setPlaceholder(new Label("No hay espacios disponibles"));
        VBox.setVgrow(spacesListView, Priority.ALWAYS);
        
        // Botones de acción
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewDetailsButton = new Button("Ver Detalles");
        viewDetailsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        viewDetailsButton.setOnAction(e -> viewSpaceDetails());
        
        Button reserveButton = new Button("Reservar");
        reserveButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        reserveButton.setOnAction(e -> createReservation());
        
        actionBox.getChildren().addAll(viewDetailsButton, reserveButton);
        
        root.getChildren().addAll(searchBox, new Separator(), spacesListView, actionBox);
        
        // Cargar espacios inicialmente
        loadSpaces();
    }
    
    private void loadSpaces() {
        // TODO: Implementar carga desde API
        // ApiClient.getInstance().get("/api/spaces/active", Space[].class);
        spacesListView.getItems().clear();
        showInfo("Cargando espacios...");
    }
    
    private void viewSpaceDetails() {
        Space selected = spacesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor seleccione un espacio");
            return;
        }
        
        // TODO: Mostrar diálogo con detalles del espacio
        showInfo("Detalles del espacio: " + selected.getNombre());
    }
    
    private void createReservation() {
        Space selected = spacesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor seleccione un espacio");
            return;
        }
        
        // TODO: Abrir diálogo de creación de reserva
        showInfo("Crear reserva para: " + selected.getNombre());
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Parent getView() {
        return root;
    }
    
    /**
     * Cell personalizada para mostrar espacios en la lista
     */
    private static class SpaceListCell extends ListCell<Space> {
        @Override
        protected void updateItem(Space space, boolean empty) {
            super.updateItem(space, empty);
            
            if (empty || space == null) {
                setText(null);
                setGraphic(null);
            } else {
                VBox content = new VBox(5);
                content.setPadding(new Insets(5));
                
                Label nameLabel = new Label(space.getNombre());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label typeLabel = new Label("Tipo: " + space.getTipo());
                Label capacityLabel = new Label("Capacidad: " + space.getCapacidad() + " personas");
                Label locationLabel = new Label("Ubicación: " + space.getUbicacion());
                Label rateLabel = new Label("Tarifa: $" + space.getTarifa());
                
                content.getChildren().addAll(nameLabel, typeLabel, capacityLabel, locationLabel, rateLabel);
                setGraphic(content);
            }
        }
    }
}
