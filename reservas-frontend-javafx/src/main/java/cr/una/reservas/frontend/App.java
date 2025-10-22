package cr.una.reservas.frontend;

import cr.una.reservas.frontend.data.ApiClient;
import cr.una.reservas.frontend.ui.login.LoginView;
import javafx.application.Application;
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
            
            // Mostrar pantalla de login
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 400, 500);
            
            primaryStage.setTitle("Sistema de Reservas Municipales");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
            
        } catch (Exception e) {
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
