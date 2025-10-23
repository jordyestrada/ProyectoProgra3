package cr.una.reservas_municipales.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO para mapear la respuesta de OpenWeatherMap One Call API 3.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponseDTO {

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lon")
    private Double lon;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("current")
    private Current current;

    @JsonProperty("daily")
    private List<Daily> daily;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        
        @JsonProperty("dt")
        private Long dt;

        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feelsLike;

        @JsonProperty("humidity")
        private Integer humidity;

        @JsonProperty("clouds")
        private Integer clouds;

        @JsonProperty("wind_speed")
        private Double windSpeed;

        @JsonProperty("weather")
        private List<Weather> weather;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Daily {
        
        @JsonProperty("dt")
        private Long dt;

        @JsonProperty("temp")
        private Temp temp;

        @JsonProperty("pop")
        private Double pop; // Probability of precipitation (0-1)

        @JsonProperty("weather")
        private List<Weather> weather;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;

        @JsonProperty("icon")
        private String icon;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Temp {
        
        @JsonProperty("day")
        private Double day;

        @JsonProperty("night")
        private Double night;

        @JsonProperty("min")
        private Double min;

        @JsonProperty("max")
        private Double max;
    }
}
