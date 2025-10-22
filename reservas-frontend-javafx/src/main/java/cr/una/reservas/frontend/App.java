package cr.una.reservas.frontend;

import cr.una.reservas.frontend.data.ApiClient;
import cr.una.reservas.frontend.util.NavigationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Aplicación principal JavaFX
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inicializar cliente API
            ApiClient.initialize("http://localhost:8080");
            
            // Configurar NavigationManager con el Stage principal
            NavigationManager.getInstance().setPrimaryStage(primaryStage);
            
            // Cargar pantalla de login desde FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 1024, 768);
            
            primaryStage.setTitle("Sistema de Reservas Municipales");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(false); // Inicia en tamaño normal, pero se puede maximizar
            primaryStage.show();
            
            System.out.println("✅ Aplicación iniciada correctamente con login.fxml");
            
        } catch (Exception e) {
            System.err.println("❌ Error al iniciar aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Limpiar recursos
        ApiClient.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
