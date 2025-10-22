package cr.una.reservas.frontend.viewmodel;

import cr.una.reservas.frontend.data.ApiClient;
import cr.una.reservas.frontend.domain.JwtResponse;
import cr.una.reservas.frontend.domain.LoginRequest;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;

/**
 * ViewModel para Login (MVVM)
 */
public class LoginViewModel {
    
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty loginInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty loginSuccessful = new SimpleBooleanProperty(false);
    
    private JwtResponse currentUser;
    
    public LoginViewModel() {
    }
    
    /**
     * Intenta autenticar al usuario
     */
    public void login() {
        if (email.get() == null || email.get().trim().isEmpty()) {
            errorMessage.set("Por favor ingrese su email");
            return;
        }
        
        if (password.get() == null || password.get().trim().isEmpty()) {
            errorMessage.set("Por favor ingrese su contraseña");
            return;
        }
        
        loginInProgress.set(true);
        errorMessage.set("");
        
        // Llamada a la API en un thread separado para no bloquear UI
        new Thread(() -> {
            try {
                LoginRequest request = new LoginRequest(email.get(), password.get());
                ApiClient.ApiResponse<JwtResponse> response = 
                    ApiClient.getInstance().post("/api/auth/login", request, JwtResponse.class);
                
                if (response.isSuccess()) {
                    currentUser = response.getData();
                    ApiClient.setAuthToken(currentUser.getToken());
                    loginSuccessful.set(true);
                } else {
                    errorMessage.set("Credenciales incorrectas");
                }
            } catch (IOException e) {
                errorMessage.set("Error de conexión: " + e.getMessage());
            } finally {
                loginInProgress.set(false);
            }
        }).start();
    }
    
    public JwtResponse getCurrentUser() {
        return currentUser;
    }
    
    // Properties para binding con la vista
    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    public BooleanProperty loginInProgressProperty() { return loginInProgress; }
    public BooleanProperty loginSuccessfulProperty() { return loginSuccessful; }
}
