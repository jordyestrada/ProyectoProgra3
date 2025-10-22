package cr.una.reservas.frontend.controllers;

import cr.una.reservas.frontend.util.FlowController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controlador Unificado de Vistas de Reservas
 * 
 * Maneja 2 vistas en un solo FXML:
 * - Formulario de Nueva Reserva (vboxBookingForm)
 * - Detalles de Espacio (vboxSpaceDetails)
 * 
 * @author Sistema de Reservas - Municipalidad de Pérez Zeledón
 */
public class BookingController {
    
    // ============================================
    // Componentes Compartidos
    // ============================================
    @FXML private Label navbarTitle;
    
    // ============================================
    // Contenedores de Vistas
    // ============================================
    @FXML private VBox vboxBookingForm;
    @FXML private VBox vboxSpaceDetails;
    
    // ============================================
    // Booking Form Components
    // ============================================
    @FXML private ImageView spaceImage;
    @FXML private Label spaceName;
    @FXML private Label spaceType;
    @FXML private Label spaceCapacity;
    @FXML private Label spaceLocation;
    @FXML private DatePicker bookingDate;
    @FXML private ComboBox<String> durationCombo;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;
    @FXML private TextField attendeesField;
    @FXML private ComboBox<String> eventTypeCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label hourlyRateLabel;
    @FXML private Label durationLabel;
    @FXML private Label totalCostLabel;
    
    // ============================================
    // Space Details Components
    // ============================================
    @FXML private ImageView spaceDetailImage;
    @FXML private Label spaceDetailName;
    @FXML private Label spaceDetailType;
    @FXML private Label spaceDetailCapacity;
    @FXML private Label spaceDetailLocation;
    @FXML private Label spaceDetailDescription;
    @FXML private VBox featuresContainer;
    @FXML private VBox availabilityCalendar;
    @FXML private Label hourlyRateDetail;
    @FXML private Label halfDayRateDetail;
    @FXML private Label fullDayRateDetail;
    @FXML private Label addressDetail;
    @FXML private VBox mapContainer;
    
    // ============================================
    // Estado
    // ============================================
    private String selectedSpaceId;
    
    /**
     * Inicialización del controlador
     */
    @FXML
    public void initialize() {
        System.out.println("✓ BookingController inicializado");
        
        // Mostrar Formulario por defecto
        showBookingForm();
        
        // Configurar combos
        setupCombos();
    }
    
    // ============================================
    // NAVEGACIÓN ENTRE VISTAS
    // ============================================
    
    /**
     * Muestra el formulario de nueva reserva
     */
    public void showBookingForm() {
        System.out.println("📝 Mostrando Formulario de Reserva");
        
        vboxBookingForm.setVisible(true);
        vboxSpaceDetails.setVisible(false);
        
        navbarTitle.setText("Nueva Reserva");
    }
    
    /**
     * Muestra los detalles del espacio
     */
    @FXML
    public void showSpaceDetails() {
        System.out.println("🏛️ Mostrando Detalles del Espacio");
        
        vboxBookingForm.setVisible(false);
        vboxSpaceDetails.setVisible(true);
        
        navbarTitle.setText("Detalles del Espacio");
        
        // Cargar detalles completos
        loadSpaceDetails();
    }
    
    // ============================================
    // ACCIONES DE NAVEGACIÓN
    // ============================================
    
    @FXML
    public void handleBack() {
        System.out.println("← Volviendo atrás");
        
        if (vboxSpaceDetails.isVisible()) {
            // Si está en detalles, volver al formulario
            showBookingForm();
        } else {
            // Si está en formulario, volver al catálogo
            FlowController.getInstance().goView("main");
        }
    }
    
    @FXML
    public void handleBookNow() {
        System.out.println("📅 Reservar Ahora desde detalles");
        showBookingForm();
    }
    
    // ============================================
    // ACCIONES BOOKING FORM
    // ============================================
    
    @FXML
    public void handleConfirmBooking() {
        System.out.println("✅ Confirmando reserva");
        
        // Validar formulario
        if (!validateForm()) {
            return;
        }
        
        // TODO: Enviar datos al API
        showAlert("Reserva Confirmada", 
                 "Tu reserva ha sido registrada exitosamente", 
                 Alert.AlertType.INFORMATION);
        
        // Volver al dashboard
        FlowController.getInstance().goView("main");
    }
    
    @FXML
    public void handleCancel() {
        System.out.println("❌ Cancelando reserva");
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancelar Reserva");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("¿Estás seguro de cancelar esta reserva?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                FlowController.getInstance().goView("main");
            }
        });
    }
    
    // ============================================
    // CONFIGURACIÓN Y VALIDACIÓN
    // ============================================
    
    private void setupCombos() {
        // Duración
        if (durationCombo != null) {
            durationCombo.getItems().addAll(
                "1 hora", 
                "2 horas", 
                "3 horas", 
                "4 horas (Medio día)", 
                "8 horas (Día completo)"
            );
        }
        
        // Tipo de evento
        if (eventTypeCombo != null) {
            eventTypeCombo.getItems().addAll(
                "Reunión",
                "Conferencia",
                "Taller",
                "Evento Social",
                "Capacitación",
                "Otro"
            );
        }
        
        // Horarios (ejemplo simplificado)
        if (startTimeCombo != null) {
            for (int i = 7; i <= 18; i++) {
                startTimeCombo.getItems().add(String.format("%02d:00", i));
                startTimeCombo.getItems().add(String.format("%02d:30", i));
            }
        }
    }
    
    private boolean validateForm() {
        // Validar fecha
        if (bookingDate.getValue() == null) {
            showAlert("Error", "Debe seleccionar una fecha", Alert.AlertType.ERROR);
            return false;
        }
        
        // Validar hora inicio
        if (startTimeCombo.getValue() == null) {
            showAlert("Error", "Debe seleccionar hora de inicio", Alert.AlertType.ERROR);
            return false;
        }
        
        // Validar asistentes
        if (attendeesField.getText().isEmpty()) {
            showAlert("Error", "Debe indicar el número de asistentes", Alert.AlertType.ERROR);
            return false;
        }
        
        // Validar teléfono
        if (phoneField.getText().isEmpty()) {
            showAlert("Error", "Debe proporcionar un teléfono de contacto", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    // ============================================
    // CARGA DE DATOS
    // ============================================
    
    /**
     * Establece el espacio seleccionado para reservar
     */
    public void setSelectedSpace(String spaceId) {
        this.selectedSpaceId = spaceId;
        loadSpaceBasicInfo();
    }
    
    private void loadSpaceBasicInfo() {
        System.out.println("📍 Cargando info básica del espacio: " + selectedSpaceId);
        // TODO: Cargar datos reales desde API
        
        if (spaceName != null) {
            spaceName.setText("Salón Municipal");
            spaceType.setText("Salón de Eventos");
            spaceCapacity.setText("👥 Capacidad: 50");
            spaceLocation.setText("📍 Centro de San Isidro");
            hourlyRateLabel.setText("₡5,000");
        }
    }
    
    private void loadSpaceDetails() {
        System.out.println("📍 Cargando detalles completos del espacio: " + selectedSpaceId);
        // TODO: Cargar datos reales desde API
        
        if (spaceDetailName != null) {
            spaceDetailName.setText("Salón Municipal");
            spaceDetailType.setText("📋 Salón de Eventos");
            spaceDetailCapacity.setText("👥 Capacidad: 50 personas");
            spaceDetailLocation.setText("📍 Centro de San Isidro");
            spaceDetailDescription.setText(
                "Amplio salón municipal ideal para eventos, conferencias y reuniones. " +
                "Cuenta con todas las comodidades necesarias para garantizar el éxito de su evento."
            );
            hourlyRateDetail.setText("₡5,000");
            halfDayRateDetail.setText("₡18,000");
            fullDayRateDetail.setText("₡35,000");
            addressDetail.setText("200m norte de la Municipalidad, Centro, San Isidro de El General");
        }
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
