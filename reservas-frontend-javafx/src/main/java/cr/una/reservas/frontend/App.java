package cr.una.reservas.frontend;

import cr.una.reservas.frontend.data.ApiClient;
import cr.una.reservas.frontend.util.FlowController;
import javafx.application.Application;
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
            
            // Inicializar FlowController con el Stage principal
            FlowController.getInstance().InitializeFlow(primaryStage);
            
            // Navegar a la pantalla de login
            FlowController.getInstance().goLogin();
            
            System.out.println("✅ Aplicación iniciada correctamente con FlowController");
            
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
