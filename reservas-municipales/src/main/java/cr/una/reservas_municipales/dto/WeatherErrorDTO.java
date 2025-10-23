package cr.una.reservas_municipales.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * DTO para errores relacionados con el servicio de clima
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherErrorDTO {

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private OffsetDateTime timestamp;

    @JsonProperty("path")
    private String path;

    @JsonProperty("status")
    private Integer status;
}
