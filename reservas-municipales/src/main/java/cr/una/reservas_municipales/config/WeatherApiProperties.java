package cr.una.reservas_municipales.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para la API del clima
 */
@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Data
public class WeatherApiProperties {

    /**
     * URL base de la API de OpenWeatherMap
     */
    private String url;

    /**
     * API Key de OpenWeatherMap
     */
    private String key;

    /**
     * Timeout en milisegundos para las peticiones HTTP
     */
    private Integer timeout;

    /**
     * Número máximo de reintentos
     */
    private Integer maxRetries;

    /**
     * Tiempo de vida del caché en segundos
     */
    private Integer cacheTtl;
}
