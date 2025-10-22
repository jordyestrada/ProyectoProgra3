package cr.una.reservas.frontend.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * Utilidad para mostrar alertas y diálogos
 * Simplifica la creación de alertas comunes
 */
public class AlertHelper {
    
    /**
     * Muestra un alert de error
     */
    public static void showError(String title, String message) {
        showAlert(title, message, AlertType.ERROR);
    }
    
    /**
     * Muestra un alert de éxito
     */
    public static void showSuccess(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION);
    }
    
    /**
     * Muestra un alert de información
     */
    public static void showInfo(String title, String message) {
        showAlert(title, message, AlertType.INFORMATION);
    }
    
    /**
     * Muestra un alert de advertencia
     */
    public static void showWarning(String title, String message) {
        showAlert(title, message, AlertType.WARNING);
    }
    
    /**
     * Muestra un alert genérico
     */
    public static void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
        System.out.println("🔔 Alert mostrado: [" + type + "] " + title + " - " + message);
    }
    
    /**
     * Muestra un diálogo de confirmación
     * @return true si el usuario confirma, false si cancela
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        boolean confirmed = result.isPresent() && result.get() == ButtonType.OK;
        System.out.println("🔔 Confirmación: " + title + " - Resultado: " + (confirmed ? "OK" : "CANCEL"));
        
        return confirmed;
    }
    
    /**
     * Muestra un diálogo de confirmación con texto personalizado
     */
    public static boolean showConfirmation(String title, String message, String confirmText, String cancelText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        ButtonType confirmButton = new ButtonType(confirmText);
        ButtonType cancelButton = new ButtonType(cancelText);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        
        boolean confirmed = result.isPresent() && result.get() == confirmButton;
        System.out.println("🔔 Confirmación: " + title + " - Resultado: " + (confirmed ? confirmText : cancelText));
        
        return confirmed;
    }
    
    /**
     * Muestra un diálogo de entrada de texto
     */
    public static Optional<String> showInputDialog(String title, String message) {
        return showInputDialog(title, message, "");
    }
    
    /**
     * Muestra un diálogo de entrada de texto con valor por defecto
     */
    public static Optional<String> showInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            System.out.println("🔔 Input dialog: " + title + " - Valor: " + result.get());
        } else {
            System.out.println("🔔 Input dialog: " + title + " - Cancelado");
        }
        
        return result;
    }
    
    /**
     * Muestra un error de validación
     */
    public static void showValidationError(String fieldName) {
        showError("Error de Validación", 
            "El campo \"" + fieldName + "\" es requerido o contiene un valor inválido.");
    }
    
    /**
     * Muestra un error de validación personalizado
     */
    public static void showValidationError(String fieldName, String reason) {
        showError("Error de Validación", 
            "Campo \"" + fieldName + "\": " + reason);
    }
    
    /**
     * Muestra un error de conexión
     */
    public static void showConnectionError() {
        showError("Error de Conexión", 
            "No se pudo conectar con el servidor. Por favor verifica tu conexión a internet e intenta nuevamente.");
    }
    
    /**
     * Muestra un error de autenticación
     */
    public static void showAuthenticationError() {
        showError("Error de Autenticación", 
            "Las credenciales son incorrectas o tu sesión ha expirado. Por favor inicia sesión nuevamente.");
    }
    
    /**
     * Muestra un error de permisos
     */
    public static void showPermissionError() {
        showError("Acceso Denegado", 
            "No tienes permisos para realizar esta acción.");
    }
    
    /**
     * Muestra un mensaje de operación exitosa
     */
    public static void showOperationSuccess(String operation) {
        showSuccess("Éxito", 
            operation + " realizado exitosamente.");
    }
    
    /**
     * Muestra un mensaje de carga/procesamiento
     */
    public static Alert showLoadingDialog(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Remover botones
        alert.getButtonTypes().clear();
        
        // No esperar respuesta
        alert.show();
        
        System.out.println("⏳ Loading dialog: " + title);
        
        return alert;
    }
}
