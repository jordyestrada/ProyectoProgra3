package cr.una.reservas_municipales.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Data
public class WeatherApiProperties {

    private String url;

    private String key;

    private Integer timeout;

    private Integer maxRetries;

    private Integer cacheTtl;
}
