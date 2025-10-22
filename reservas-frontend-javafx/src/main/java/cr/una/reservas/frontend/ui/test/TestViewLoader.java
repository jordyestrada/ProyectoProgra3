package cr.una.reservas.frontend.ui.test;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

/**
 * Clase utilitaria para cargar la vista de prueba FXML
 */
public class TestViewLoader {
    
    /**
     * Carga el FXML de prueba y retorna el Parent
     */
    public static Parent load() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            TestViewLoader.class.getResource("/fxml/test.fxml")
        );
        
        Parent root = loader.load();
        
        // Opcional: obtener el controller si necesitas hacer algo con él
        TestController controller = loader.getController();
        System.out.println("✅ FXML cargado correctamente");
        
        return root;
    }
}
