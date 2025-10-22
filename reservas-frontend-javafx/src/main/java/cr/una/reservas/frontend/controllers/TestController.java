package cr.una.reservas.frontend.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import cr.una.reservas.frontend.util.FlowController;

/**
 * Controller de prueba para verificar FXML + SceneBuilder
 */
public class TestController {
    
    @FXML private TextField inputField;
    @FXML private Button testButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;
    @FXML private Label resultLabel;
    
    /**
     * Método initialize se llama automáticamente después de cargar el FXML
     */
    @FXML
    public void initialize() {
        System.out.println("✅ TestController inicializado correctamente");
        System.out.println("✅ FXML cargado exitosamente");
    }
    
    /**
     * Maneja el botón "Probar"
     */
    @FXML
    private void handleTest() {
        String texto = inputField.getText();
        
        if (texto == null || texto.trim().isEmpty()) {
            resultLabel.setText("⚠️ Por favor ingresa un texto");
            resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        } else {
            resultLabel.setText("✅ Recibido: \"" + texto + "\"");
            resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        }
        
        System.out.println("🔍 Texto ingresado: " + texto);
    }
    
    /**
     * Maneja el botón "Limpiar"
     */
    @FXML
    private void handleClear() {
        inputField.clear();
        resultLabel.setText("🗑️ Campo limpiado");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        System.out.println("🗑️ Campo limpiado");
    }
    
    /**
     * Maneja el botón "Volver al Login"
     */
    @FXML
    private void handleBack() {
        try {
            System.out.println("← Volviendo a Login");
            FlowController.getInstance().goLogin();
        } catch (Exception e) {
            System.err.println("Error al volver: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
