package cr.una.reservas.frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

/**
 * Gestiona la navegación entre pantallas
 * Centraliza el cambio de escenas y diálogos
 */
public class NavigationManager {
    
    private static NavigationManager instance;
    private Stage primaryStage;
    
    /**
     * Constructor privado (Singleton)
     */
    private NavigationManager() {}
    
    /**
     * Obtiene la instancia única de NavigationManager
     */
    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }
    
    /**
     * Establece el Stage principal de la aplicación
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        System.out.println("✅ Primary Stage configurado");
    }
    
    /**
     * Obtiene el Stage principal
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Navega a una pantalla
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título de la ventana
     */
    public void navigateTo(String fxmlPath, String title) {
        navigateTo(fxmlPath, title, 1440, 900);
    }
    
    /**
     * Navega a una pantalla con tamaño personalizado
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título de la ventana
     * @param width Ancho de la ventana
     * @param height Alto de la ventana
     */
    public void navigateTo(String fxmlPath, String title, int width, int height) {
        try {
            System.out.println("🔄 Navegando a: " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, width, height);
            
            if (primaryStage != null) {
                primaryStage.setScene(scene);
                primaryStage.setTitle("Sistema de Reservas - " + title);
                primaryStage.setResizable(true); // Permite redimensionar
                primaryStage.show();
                
                System.out.println("✅ Navegación exitosa a: " + title);
            } else {
                System.err.println("❌ Error: Primary Stage no configurado");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al navegar a " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            AlertHelper.showError("Error de Navegación", 
                "No se pudo cargar la pantalla: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un diálogo modal
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título del diálogo
     * @return El Stage del diálogo
     */
    public Stage showDialog(String fxmlPath, String title) {
        return showDialog(fxmlPath, title, 600, 400);
    }
    
    /**
     * Muestra un diálogo modal con tamaño personalizado
     * @param fxmlPath Ruta del archivo FXML
     * @param title Título del diálogo
     * @param width Ancho del diálogo
     * @param height Alto del diálogo
     * @return El Stage del diálogo
     */
    public Stage showDialog(String fxmlPath, String title, int width, int height) {
        try {
            System.out.println("📋 Abriendo diálogo: " + fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);
            
            Scene scene = new Scene(root, width, height);
            dialogStage.setScene(scene);
            
            System.out.println("✅ Diálogo creado: " + title);
            
            return dialogStage;
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear diálogo " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            AlertHelper.showError("Error", 
                "No se pudo abrir el diálogo: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Navega al login y cierra la sesión
     */
    public void navigateToLogin() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/login.fxml", "Login", 1024, 768);
    }
    
    /**
     * Navega al dashboard
     */
    public void navigateToDashboard() {
        navigateTo("/fxml/dashboard.fxml", "Dashboard");
        // Maximizar la ventana del dashboard
        if (primaryStage != null) {
            primaryStage.setMaximized(true);
        }
    }
    
    /**
     * Navega al catálogo de espacios
     */
    public void navigateToSpaces() {
        navigateTo("/fxml/spaces-catalog.fxml", "Catálogo de Espacios");
    }
    
    /**
     * Navega a mis reservas
     */
    public void navigateToMyBookings() {
        navigateTo("/fxml/my-bookings.fxml", "Mis Reservas");
    }
    
    /**
     * Navega a admin - espacios
     */
    public void navigateToAdminSpaces() {
        navigateTo("/fxml/admin-spaces.fxml", "Administración de Espacios");
    }
    
    /**
     * Navega a admin - reportes
     */
    public void navigateToAdminReports() {
        navigateTo("/fxml/admin-reports.fxml", "Reportes Administrativos");
    }
}
