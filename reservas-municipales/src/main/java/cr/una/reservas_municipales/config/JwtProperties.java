package cr.una.reservas_municipales.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "mySecretKey123456789012345678901234567890"; // 256-bit secret
    private long expiration = 86400000; // 24 hours in milliseconds
    private String issuer = "reservas-municipales";
}