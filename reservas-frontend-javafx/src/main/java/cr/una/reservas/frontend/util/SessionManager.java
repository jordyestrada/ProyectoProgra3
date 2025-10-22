package cr.una.reservas.frontend.util;

import cr.una.reservas.frontend.domain.User;
import java.util.UUID;

/**
 * Gestiona la sesión del usuario actual
 * Patrón Singleton para acceso global
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private User currentUser;
    private String jwtToken;
    private boolean isLoggedIn;
    
    /**
     * Constructor privado (Singleton)
     */
    private SessionManager() {
        this.isLoggedIn = false;
    }
    
    /**
     * Obtiene la instancia única de SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Inicia sesión con un usuario y token
     */
    public void login(User user, String token) {
        this.currentUser = user;
        this.jwtToken = token;
        this.isLoggedIn = true;
        
        System.out.println("✅ Sesión iniciada para: " + user.getEmail());
        System.out.println("🔑 Token JWT almacenado");
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        System.out.println("👋 Cerrando sesión de: " + (currentUser != null ? currentUser.getEmail() : "Usuario"));
        
        this.currentUser = null;
        this.jwtToken = null;
        this.isLoggedIn = false;
        
        System.out.println("✅ Sesión cerrada");
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public boolean isLoggedIn() {
        return isLoggedIn && currentUser != null && jwtToken != null;
    }
    
    /**
     * Obtiene el usuario actual
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Obtiene el token JWT actual
     */
    public String getJwtToken() {
        return jwtToken;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        if (currentUser == null) {
            return false;
        }
        
        // TODO: Verificar el rol del usuario
        // return currentUser.getRole().equals("ROLE_ADMIN");
        
        return false; // Por ahora
    }
    
    /**
     * Obtiene el ID del usuario actual
     */
    public UUID getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    public String getCurrentUserName() {
        if (currentUser == null) {
            return "Usuario";
        }
        
        String fullName = currentUser.getNombre() + " " + currentUser.getApellido();
        return fullName.trim().isEmpty() ? currentUser.getEmail() : fullName;
    }
    
    /**
     * Obtiene el email del usuario actual
     */
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }
    
    /**
     * Actualiza el token JWT
     */
    public void updateToken(String newToken) {
        this.jwtToken = newToken;
        System.out.println("🔄 Token JWT actualizado");
    }
    
    /**
     * Actualiza los datos del usuario actual
     */
    public void updateCurrentUser(User updatedUser) {
        this.currentUser = updatedUser;
        System.out.println("🔄 Datos de usuario actualizados");
    }
    
    /**
     * Limpia todos los datos de la sesión
     */
    public void clear() {
        logout();
    }
}
