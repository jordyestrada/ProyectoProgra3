package cr.una.reservas_municipales.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO de respuesta del clima para el cliente.
 * Incluye información meteorológica y recomendaciones para actividades al aire libre.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {

    /**
     * Ubicación consultada (ciudad, país)
     */
    @JsonProperty("location")
    private String location;

    /**
     * Temperatura actual en grados Celsius
     */
    @JsonProperty("temperature")
    private Double temperature;

    /**
     * Sensación térmica en grados Celsius
     */
    @JsonProperty("feels_like")
    private Double feelsLike;

    /**
     * Descripción del clima (ej: "cielo claro", "lluvia ligera")
     */
    @JsonProperty("description")
    private String description;

    /**
     * Humedad relativa (%)
     */
    @JsonProperty("humidity")
    private Integer humidity;

    /**
     * Velocidad del viento (m/s)
     */
    @JsonProperty("wind_speed")
    private Double windSpeed;

    /**
     * Nubosidad (%)
     */
    @JsonProperty("cloudiness")
    private Integer cloudiness;

    /**
     * Probabilidad de lluvia (0-100%)
     */
    @JsonProperty("rain_probability")
    private Double rainProbability;

    /**
     * Indica si las condiciones son aptas para actividades al aire libre
     */
    @JsonProperty("is_outdoor_friendly")
    private Boolean isOutdoorFriendly;

    /**
     * Recomendación personalizada basada en las condiciones
     */
    @JsonProperty("recommendation")
    private String recommendation;

    /**
     * Fuente de datos: API, CACHE, FALLBACK
     */
    @JsonProperty("data_source")
    private String dataSource;

    /**
     * Timestamp de cuando se obtuvo la información
     */
    @JsonProperty("fetched_at")
    private OffsetDateTime fetchedAt;

    /**
     * Latitud de la ubicación
     */
    @JsonProperty("latitude")
    private Double latitude;

    /**
     * Longitud de la ubicación
     */
    @JsonProperty("longitude")
    private Double longitude;
}
