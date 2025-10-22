package cr.una.reservas.frontend.controllers;

import javafx.stage.Stage;

/**
 * Interfaz base para todos los controladores
 * Define métodos comunes que deben implementar los controladores
 */
public interface Controller {
    
    /**
     * Inicializa el controller
     */
    void initialize();
    
    /**
     * Obtiene el Stage asociado al controller
     */
    Stage getStage();
    
    /**
     * Establece el Stage para el controller
     */
    void setStage(Stage stage);
}
