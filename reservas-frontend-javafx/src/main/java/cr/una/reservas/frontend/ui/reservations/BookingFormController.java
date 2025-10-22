package cr.una.reservas.frontend.ui.reservations;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;

/**
 * Controller para el formulario de reserva (wizard de 3 pasos)
 * Paso 1: Fecha y hora
 * Paso 2: Detalles (propósito, personas)
 * Paso 3: Confirmación y resumen
 */
public class BookingFormController {
    
    // Stepper (pasos)
    @FXML private HBox stepperContainer;
    @FXML private Label step1Label;
    @FXML private Label step2Label;
    @FXML private Label step3Label;
    
    // Contenedores de pasos
    @FXML private VBox step1Container;
    @FXML private VBox step2Container;
    @FXML private VBox step3Container;
    
    // Step 1: Fecha y hora
    @FXML private DatePicker dateField;
    @FXML private ComboBox<String> startTimeCombo;
    @FXML private ComboBox<String> endTimeCombo;
    
    // Step 2: Detalles
    @FXML private TextField purposeField;
    @FXML private Spinner<Integer> peopleCountSpinner;
    @FXML private TextArea notesArea;
    
    // Step 3: Resumen
    @FXML private Label summarySpaceLabel;
    @FXML private Label summaryDateLabel;
    @FXML private Label summaryTimeLabel;
    @FXML private Label summaryPurposeLabel;
    @FXML private Label summaryPeopleLabel;
    @FXML private Label summaryPriceLabel;
    
    // Botones de navegación
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    
    private int currentStep = 1;
    private Long spaceId;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ BookingFormController inicializado");
        
        // Configurar time pickers
        setupTimePickers();
        
        // Configurar spinner
        setupPeopleSpinner();
        
        // Mostrar primer paso
        showStep(1);
    }
    
    /**
     * Configura los ComboBox de hora
     */
    private void setupTimePickers() {
        if (startTimeCombo != null && endTimeCombo != null) {
            for (int hour = 7; hour <= 22; hour++) {
                String time = String.format("%02d:00", hour);
                startTimeCombo.getItems().add(time);
                endTimeCombo.getItems().add(time);
            }
            startTimeCombo.setValue("08:00");
            endTimeCombo.setValue("17:00");
        }
    }
    
    /**
     * Configura el Spinner de personas
     */
    private void setupPeopleSpinner() {
        if (peopleCountSpinner != null) {
            SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 500, 10);
            peopleCountSpinner.setValueFactory(valueFactory);
        }
    }
    
    /**
     * Establece el ID del espacio a reservar
     */
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
        loadSpaceInfo();
    }
    
    /**
     * Carga información del espacio
     */
    private void loadSpaceInfo() {
        System.out.println("📍 Cargando info del espacio ID: " + spaceId);
        // TODO: Llamar a GET /api/spaces/{id}
    }
    
    /**
     * Muestra un paso específico
     */
    private void showStep(int step) {
        currentStep = step;
        
        // Ocultar todos los pasos
        if (step1Container != null) step1Container.setVisible(false);
        if (step2Container != null) step2Container.setVisible(false);
        if (step3Container != null) step3Container.setVisible(false);
        
        // Mostrar paso actual
        if (step == 1 && step1Container != null) {
            step1Container.setVisible(true);
            if (previousButton != null) previousButton.setVisible(false);
            if (nextButton != null) nextButton.setVisible(true);
            if (confirmButton != null) confirmButton.setVisible(false);
        } else if (step == 2 && step2Container != null) {
            step2Container.setVisible(true);
            if (previousButton != null) previousButton.setVisible(true);
            if (nextButton != null) nextButton.setVisible(true);
            if (confirmButton != null) confirmButton.setVisible(false);
        } else if (step == 3 && step3Container != null) {
            step3Container.setVisible(true);
            if (previousButton != null) previousButton.setVisible(true);
            if (nextButton != null) nextButton.setVisible(false);
            if (confirmButton != null) confirmButton.setVisible(true);
            loadSummary();
        }
        
        updateStepperUI();
        System.out.println("📍 Mostrando paso " + step);
    }
    
    /**
     * Actualiza la UI del stepper
     */
    private void updateStepperUI() {
        // TODO: Actualizar estilos de los círculos del stepper
        // Agregar clase "step-circle-active" al paso actual
    }
    
    /**
     * Maneja el botón "Siguiente"
     */
    @FXML
    private void handleNext() {
        if (currentStep == 1) {
            if (validateStep1()) {
                showStep(2);
            }
        } else if (currentStep == 2) {
            if (validateStep2()) {
                showStep(3);
            }
        }
    }
    
    /**
     * Maneja el botón "Anterior"
     */
    @FXML
    private void handlePrevious() {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    }
    
    /**
     * Valida el paso 1
     */
    private boolean validateStep1() {
        LocalDate date = dateField != null ? dateField.getValue() : null;
        
        if (date == null) {
            showAlert("Error", "Por favor selecciona una fecha", Alert.AlertType.ERROR);
            return false;
        }
        
        if (date.isBefore(LocalDate.now())) {
            showAlert("Error", "La fecha no puede ser en el pasado", Alert.AlertType.ERROR);
            return false;
        }
        
        // TODO: Validar disponibilidad
        // GET /api/spaces/{id}/availability?date={date}
        
        return true;
    }
    
    /**
     * Valida el paso 2
     */
    private boolean validateStep2() {
        String purpose = purposeField != null ? purposeField.getText() : "";
        
        if (purpose.trim().isEmpty()) {
            showAlert("Error", "Por favor ingresa el propósito de la reserva", Alert.AlertType.ERROR);
            return false;
        }
        
        return true;
    }
    
    /**
     * Carga el resumen en el paso 3
     */
    private void loadSummary() {
        if (summaryDateLabel != null) {
            LocalDate date = dateField != null ? dateField.getValue() : null;
            summaryDateLabel.setText(date != null ? date.toString() : "N/A");
        }
        
        if (summaryTimeLabel != null) {
            String start = startTimeCombo != null ? startTimeCombo.getValue() : "";
            String end = endTimeCombo != null ? endTimeCombo.getValue() : "";
            summaryTimeLabel.setText(start + " - " + end);
        }
        
        if (summaryPurposeLabel != null) {
            String purpose = purposeField != null ? purposeField.getText() : "";
            summaryPurposeLabel.setText(purpose);
        }
        
        if (summaryPeopleLabel != null) {
            int people = peopleCountSpinner != null ? peopleCountSpinner.getValue() : 0;
            summaryPeopleLabel.setText(people + " personas");
        }
        
        if (summaryPriceLabel != null) {
            summaryPriceLabel.setText("₡150,000");
        }
    }
    
    /**
     * Maneja el botón "Confirmar"
     */
    @FXML
    private void handleConfirm() {
        System.out.println("✅ Confirmando reserva...");
        
        // TODO: Crear reserva
        // POST /api/reservations
        
        // Mock - Simular éxito
        showAlert("Éxito", "¡Reserva creada exitosamente!", Alert.AlertType.INFORMATION);
        navigateTo("/fxml/my-bookings.fxml", "Mis Reservas");
    }
    
    /**
     * Maneja el botón "Cancelar"
     */
    @FXML
    private void handleCancel() {
        System.out.println("❌ Cancelando reserva");
        navigateTo("/fxml/space-details.fxml", "Detalle del Espacio");
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
    
    /**
     * Navega a otra pantalla
     */
    private void navigateTo(String fxmlPath, String title) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) cancelButton.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1440, 900);
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + title);
        } catch (Exception e) {
            System.err.println("❌ Error al navegar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
