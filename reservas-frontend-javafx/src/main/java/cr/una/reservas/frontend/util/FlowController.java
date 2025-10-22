package cr.una.reservas.frontend.util;

import cr.una.reservas.frontend.App;
import cr.una.reservas.frontend.controllers.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador de flujo de navegación entre vistas
 * Patrón Singleton
 */
public class FlowController {

    private static FlowController INSTANCE = null;
    private static Stage mainStage;
    private static final Logger LOGGER = Logger.getLogger(FlowController.class.getName());

    private FlowController() {
    }

    private static void createInstance() {
        if (INSTANCE == null) {
            synchronized (FlowController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FlowController();
                }
            }
        }
    }

    public static FlowController getInstance() {
        if (INSTANCE == null) {
            createInstance();
        }
        return INSTANCE;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void InitializeFlow(Stage stage) {
        getInstance();
        FlowController.mainStage = stage;
        LOGGER.info("✅ FlowController inicializado");
    }

    /**
     * Obtiene o crea un FXMLLoader para una vista
     */
    private FXMLLoader getLoader(String name) {
        try {
            String fxmlPath = "/fxml/" + name + ".fxml";
            LOGGER.info("📂 Cargando vista: " + fxmlPath);
            
            // Crear nuevo loader cada vez para evitar problemas de reutilización
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlPath));
            loader.load();
            
            LOGGER.info("✅ Vista cargada exitosamente: " + name);
            return loader;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error cargando vista [" + name + "]", ex);
            return null;
        }
    }

    /**
     * Navega a la vista de login
     */
    public void goLogin() {
        try {
            LOGGER.info("🔄 Navegando a Login...");
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1024, 768);
            mainStage.setScene(scene);
            mainStage.setTitle("Sistema de Reservas - Iniciar Sesión");
            mainStage.setMaximized(true);
            mainStage.centerOnScreen();
            mainStage.show();
            
            LOGGER.info("✅ Login cargado exitosamente");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error navegando a Login", ex);
        }
    }

    /**
     * Navega al dashboard principal (nueva vista consolidada main.fxml)
     */
    public void goMain() {
        goView("main");
    }

    /**
     * Navega a una vista específica (pantalla completa)
     */
    public void goView(String viewName) {
        try {
            LOGGER.info("🔄 Navegando a vista: " + viewName);
            
            FXMLLoader loader = getLoader(viewName);
            if (loader == null) {
                LOGGER.severe("❌ No se pudo cargar el loader para: " + viewName);
                return;
            }
            
            Parent root = loader.getRoot();
            
            // Reusar la Scene existente en lugar de crear una nueva
            Scene currentScene = mainStage.getScene();
            if (currentScene != null) {
                currentScene.setRoot(root);
            } else {
                Scene scene = new Scene(root, 1440, 900);
                mainStage.setScene(scene);
            }
            
            mainStage.setTitle("Sistema de Reservas - " + getTitleForView(viewName));
            mainStage.setMaximized(true);
            mainStage.show();
            
            // Inicializar controller si implementa Controller
            Object controller = loader.getController();
            if (controller instanceof Controller) {
                ((Controller) controller).initialize();
            }
            
            LOGGER.info("✅ Vista cargada: " + viewName);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error navegando a vista [" + viewName + "]", ex);
        }
    }

    /**
     * Abre una vista en una ventana modal
     */
    public void goViewInWindowModal(String viewName, Stage parentStage, Boolean resizable) {
        try {
            FXMLLoader loader = getLoader(viewName);
            if (loader == null) {
                return;
            }

            Parent root = loader.getRoot();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(getTitleForView(viewName));
            stage.setResizable(resizable);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(parentStage);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error abriendo ventana modal", ex);
        }
    }

    /**
     * Abre una vista en una nueva ventana
     */
    public void goViewInWindow(String viewName) {
        try {
            FXMLLoader loader = getLoader(viewName);
            if (loader == null) {
                return;
            }

            Parent root = loader.getRoot();
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(getTitleForView(viewName));
            stage.centerOnScreen();
            
            // Hacer que cierre toda la app al cerrar esta ventana
            stage.setOnCloseRequest((WindowEvent event) -> {
                mainStage.close();
            });
            
            stage.show();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "❌ Error abriendo ventana", ex);
        }
    }

    /**
     * Obtiene el stage principal
     */
    public Stage getMainStage() {
        return mainStage;
    }

    /**
     * Establece el stage principal
     */
    public void setMainStage(Stage stage) {
        FlowController.mainStage = stage;
    }

    /**
     * Obtiene el título de la ventana según la vista
     */
    private String getTitleForView(String viewName) {
        return switch (viewName) {
            case "login" -> "Iniciar Sesión";
            // Nueva estructura consolidada
            case "main" -> "Sistema de Reservas";
            case "admin" -> "Panel Administrativo";
            case "booking" -> "Nueva Reserva";
            // Vistas antiguas (para compatibilidad temporal)
            case "dashboard" -> "Dashboard";
            case "spaces-catalog" -> "Catálogo de Espacios";
            case "space-details" -> "Detalles del Espacio";
            case "my-bookings" -> "Mis Reservas";
            case "booking-form" -> "Nueva Reserva";
            case "admin-spaces" -> "Gestión de Espacios";
            case "admin-reports" -> "Reportes";
            default -> "Sistema de Reservas";
        };
    }
}
