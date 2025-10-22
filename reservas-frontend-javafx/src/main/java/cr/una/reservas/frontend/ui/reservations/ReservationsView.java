package cr.una.reservas.frontend.ui.reservations;

import cr.una.reservas.frontend.domain.JwtResponse;
import cr.una.reservas.frontend.domain.Reservation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Vista de Reservaciones del usuario
 */
public class ReservationsView {
    
    private final VBox root;
    private final JwtResponse currentUser;
    
    private TableView<Reservation> reservationsTable;
    private ComboBox<String> statusFilter;
    
    public ReservationsView(JwtResponse currentUser) {
        this.currentUser = currentUser;
        this.root = new VBox(10);
        initializeUI();
    }
    
    private void initializeUI() {
        root.setPadding(new Insets(20));
        
        // Filtros
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filtrar por estado:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Todas", "PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA");
        statusFilter.setValue("Todas");
        statusFilter.setOnAction(e -> filterReservations());
        
        Button refreshButton = new Button("Actualizar");
        refreshButton.setOnAction(e -> loadReservations());
        
        filterBox.getChildren().addAll(filterLabel, statusFilter, refreshButton);
        
        // Tabla de reservas
        reservationsTable = new TableView<>();
        reservationsTable.setPlaceholder(new Label("No tiene reservas"));
        VBox.setVgrow(reservationsTable, Priority.ALWAYS);
        
        // Columnas
        TableColumn<Reservation, String> spaceCol = new TableColumn<>("Espacio");
        TableColumn<Reservation, String> startCol = new TableColumn<>("Fecha Inicio");
        TableColumn<Reservation, String> endCol = new TableColumn<>("Fecha Fin");
        TableColumn<Reservation, String> statusCol = new TableColumn<>("Estado");
        TableColumn<Reservation, String> totalCol = new TableColumn<>("Monto Total");
        
        // Agregar columnas una por una para evitar warning de varargs genéricos
        reservationsTable.getColumns().add(spaceCol);
        reservationsTable.getColumns().add(startCol);
        reservationsTable.getColumns().add(endCol);
        reservationsTable.getColumns().add(statusCol);
        reservationsTable.getColumns().add(totalCol);
        
        // Botones de acción
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button viewDetailsButton = new Button("Ver Detalles");
        viewDetailsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        viewDetailsButton.setOnAction(e -> viewDetails());
        
        Button viewQRButton = new Button("Ver QR");
        viewQRButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        viewQRButton.setOnAction(e -> viewQRCode());
        
        Button cancelButton = new Button("Cancelar Reserva");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> cancelReservation());
        
        actionBox.getChildren().addAll(viewDetailsButton, viewQRButton, cancelButton);
        
        root.getChildren().addAll(filterBox, new Separator(), reservationsTable, actionBox);
        
        // Cargar reservas
        loadReservations();
    }
    
    private void loadReservations() {
        // TODO: Implementar carga desde API
        // ApiClient.getInstance().get("/api/reservations/user/" + currentUser.getId(), Reservation[].class);
        showInfo("Cargando reservas...");
    }
    
    private void filterReservations() {
        // TODO: Filtrar reservas por estado
    }
    
    private void viewDetails() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor seleccione una reserva");
            return;
        }
        
        // TODO: Mostrar diálogo con detalles
        showInfo("Detalles de reserva ID: " + selected.getId());
    }
    
    private void viewQRCode() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor seleccione una reserva");
            return;
        }
        
        // TODO: Mostrar QR de la reserva
        showInfo("Mostrando código QR...");
    }
    
    private void cancelReservation() {
        Reservation selected = reservationsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Por favor seleccione una reserva");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Reserva");
        confirm.setHeaderText("¿Está seguro que desea cancelar esta reserva?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Llamar API para cancelar
                showInfo("Reserva cancelada");
            }
        });
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
}
