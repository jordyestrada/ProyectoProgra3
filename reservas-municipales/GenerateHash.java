import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        // Test the existing hash
        String existingHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches(password, existingHash);
        System.out.println("\nTesting existing hash:");
        System.out.println("Existing hash: " + existingHash);
        System.out.println("Matches 'admin123': " + matches);
    }
}
