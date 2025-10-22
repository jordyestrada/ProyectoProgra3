package cr.una.reservas.frontend.ui.login;

import cr.una.reservas.frontend.ui.main.MainView;
import cr.una.reservas.frontend.ui.test.TestViewLoader;
import cr.una.reservas.frontend.viewmodel.LoginViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Vista de Login
 */
public class LoginView {
    
    private final VBox root;
    private final LoginViewModel viewModel;
    
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private ProgressIndicator progressIndicator;
    
    public LoginView() {
        this.viewModel = new LoginViewModel();
        this.root = new VBox(15);
        initializeUI();
        bindViewModel();
    }
    
    private void initializeUI() {
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label titleLabel = new Label("Sistema de Reservas Municipales");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Iniciar Sesión");
        subtitleLabel.setStyle("-fx-font-size: 14px;");
        
        // Email
        Label emailLabel = new Label("Email:");
        emailField = new TextField();
        emailField.setPromptText("usuario@example.com");
        emailField.setMaxWidth(300);
        
        // Password
        Label passwordLabel = new Label("Contraseña:");
        passwordField = new PasswordField();
        passwordField.setPromptText("********");
        passwordField.setMaxWidth(300);
        
        // Login button
        loginButton = new Button("Ingresar");
        loginButton.setMaxWidth(300);
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setOnAction(e -> viewModel.login());
        
        // Test FXML button
        Button testFxmlButton = new Button("🔧 Probar FXML + SceneBuilder");
        testFxmlButton.setMaxWidth(300);
        testFxmlButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 12px;");
        testFxmlButton.setOnAction(e -> openTestView());
        
        // Progress indicator
        progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(30, 30);
        progressIndicator.setVisible(false);
        
        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);
        
        // Enter key support
        passwordField.setOnAction(e -> viewModel.login());
        
        // Separator
        Separator separator = new Separator();
        separator.setMaxWidth(300);
        
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            emailLabel,
            emailField,
            passwordLabel,
            passwordField,
            loginButton,
            progressIndicator,
            errorLabel,
            separator,
            testFxmlButton
        );
    }
    
    private void bindViewModel() {
        // Binding bidireccional de campos
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        
        // Binding de estado de login
        viewModel.loginInProgressProperty().addListener((obs, oldVal, newVal) -> {
            loginButton.setDisable(newVal);
            progressIndicator.setVisible(newVal);
        });
        
        // Binding de mensajes de error
        viewModel.errorMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                errorLabel.setText(newVal);
                errorLabel.setVisible(true);
            } else {
                errorLabel.setVisible(false);
            }
        });
        
        // Binding de login exitoso
        viewModel.loginSuccessfulProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                openMainView();
            }
        });
    }
    
    private void openMainView() {
        try {
            MainView mainView = new MainView(viewModel.getCurrentUser());
            Scene scene = new Scene(mainView.getView(), 1200, 800);
            
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Sistema de Reservas - " + viewModel.getCurrentUser().getEmail());
            stage.setMaximized(true);
            
        } catch (Exception e) {
            errorLabel.setText("Error al abrir ventana principal: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
    
    private void openTestView() {
        try {
            Parent testView = TestViewLoader.load();
            Scene scene = new Scene(testView, 500, 400);
            
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Prueba FXML + SceneBuilder");
            
            System.out.println("✅ Vista de prueba FXML cargada exitosamente");
            
        } catch (Exception e) {
            errorLabel.setText("Error al cargar FXML: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }
    
    public Parent getView() {
        return root;
    }
}
