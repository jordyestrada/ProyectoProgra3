package cr.una.reservas.frontend.controllers;

import cr.una.reservas.frontend.util.AlertHelper;
import cr.una.reservas.frontend.util.FlowController;
import cr.una.reservas.frontend.util.SessionManager;
import cr.una.reservas.frontend.domain.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller para la pantalla de Login
 * Maneja la autenticación con Azure AD
 */
public class LoginController {
    
    @FXML private VBox rootContainer;
    @FXML private ImageView logoImageView;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Hyperlink helpLink;
    @FXML private Label footerLabel;
    
    /**
     * Inicialización del controller
     */
    @FXML
    public void initialize() {
        System.out.println("✅ LoginController inicializado");
        
        // Ocultar indicador de carga
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }
    }
    
    /**
     * Maneja el botón de Login con Azure AD
     */
    @FXML
    private void handleLogin() {
        System.out.println("🔐 Iniciando login con Azure AD...");
        
        showLoading(true);
        
        // Simular login en un thread separado para no bloquear UI
        new Thread(() -> {
            try {
                // TODO: Llamar a la API de login
                // ApiClient.login(email, password);
                
                // Por ahora: Login simulado con delay
                Thread.sleep(1000);
                
                // Crear usuario mock
                User mockUser = new User();
                mockUser.setEmail("admin@municipalidad.go.cr");
                mockUser.setNombre("Admin");
                mockUser.setApellido("Sistema");
                mockUser.setRol("ADMIN");
                
                // Guardar en sesión
                SessionManager.getInstance().login(mockUser, "mock-jwt-token-12345");
                
                // Navegar al dashboard en el thread de JavaFX
                Platform.runLater(() -> {
                    showLoading(false);
                    System.out.println("✅ Login exitoso, navegando al dashboard...");
                    FlowController.getInstance().goMain();
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    AlertHelper.showError("Error", "No se pudo iniciar sesión: " + e.getMessage());
                    System.err.println("❌ Error en login: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Maneja el link de ayuda
     */
    @FXML
    private void handleHelp() {
        AlertHelper.showInfo("Ayuda", 
            "Para soporte técnico, contacta a:\n\n" +
            "📧 Email: soporte@municipalidad.go.cr\n" +
            "📞 Teléfono: 2771-3000 ext. 100");
        System.out.println("ℹ️ Usuario solicitó ayuda");
    }
    
    /**
     * Muestra/oculta el indicador de carga
     */
    private void showLoading(boolean show) {
        if (progressIndicator != null) {
            progressIndicator.setVisible(show);
        }
        if (loginButton != null) {
            loginButton.setDisable(show);
        }
    }
}
