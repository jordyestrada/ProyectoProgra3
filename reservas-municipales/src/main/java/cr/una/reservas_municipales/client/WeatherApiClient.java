package cr.una.reservas_municipales.client;

import cr.una.reservas_municipales.config.WeatherApiProperties;
import cr.una.reservas_municipales.dto.OpenWeatherResponseDTO;
import cr.una.reservas_municipales.exception.WeatherApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para consumir OpenWeatherMap API
 * Incluye retry, circuit breaker y fallback
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {

    @Qualifier("weatherWebClient")
    private final WebClient weatherWebClient;
    
    private final WeatherApiProperties weatherApiProperties;

    private static final String ONECALL_ENDPOINT = "/onecall";
    private static final String GEOCODING_BASE_URL = "http://api.openweathermap.org/geo/1.0";
    
    /**
     * Obtiene información del clima por coordenadas geográficas
     * @param latitude Latitud
     * @param longitude Longitud
     * @return Datos meteorológicos
     */
    @Retry(name = "weatherApi", fallbackMethod = "getWeatherByCoordinatesFallback")
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "getWeatherByCoordinatesFallback")
    public OpenWeatherResponseDTO getWeatherByCoordinates(Double latitude, Double longitude) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Consultando clima para coordenadas: lat={}, lon={}", latitude, longitude);
            
            OpenWeatherResponseDTO response = weatherWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ONECALL_ENDPOINT)
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", weatherApiProperties.getKey())
                            .queryParam("units", "metric")
                            .queryParam("lang", "es")
                            .queryParam("exclude", "minutely,hourly,alerts")
                            .build())
                    .retrieve()
                    .bodyToMono(OpenWeatherResponseDTO.class)
                    .timeout(Duration.ofMillis(weatherApiProperties.getTimeout()))
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            
            if (duration > 2000) {
                log.warn("Weather API response time {}ms exceeded 2s threshold", duration);
            } else {
                log.info("Weather data fetched successfully in {}ms", duration);
            }
            
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("Error calling Weather API: HTTP {}", e.getStatusCode(), e);
            throw new WeatherApiException("Error en la API del clima: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Weather API", e);
            throw new WeatherApiException("Error inesperado al consultar el clima", e);
        }
    }

    /**
     * Fallback para getWeatherByCoordinates
     */
    private OpenWeatherResponseDTO getWeatherByCoordinatesFallback(Double latitude, Double longitude, Throwable t) {
        log.warn("Using fallback for coordinates: lat={}, lon={}, reason={}", 
                latitude, longitude, t.getMessage());
        
        // Retornar respuesta genérica de fallback
        return createFallbackResponse(latitude, longitude);
    }

    /**
     * Obtiene información del clima por nombre de ciudad
     * @param location Nombre de la ciudad (ej: "San Jose,CR")
     * @return Datos meteorológicos
     */
    @Retry(name = "weatherApi", fallbackMethod = "getWeatherByLocationFallback")
    @CircuitBreaker(name = "weatherApi", fallbackMethod = "getWeatherByLocationFallback")
    public OpenWeatherResponseDTO getWeatherByLocation(String location) {
        try {
            log.info("Buscando coordenadas para location: {}", location);
            
            // Primero obtener coordenadas con Geocoding API
            Map<String, Double> coordinates = getCoordinatesByLocation(location);
            
            if (coordinates == null || coordinates.get("lat") == null || coordinates.get("lon") == null) {
                throw new WeatherApiException("No se pudieron obtener coordenadas para: " + location);
            }
            
            // Luego obtener clima con esas coordenadas
            return getWeatherByCoordinates(coordinates.get("lat"), coordinates.get("lon"));
            
        } catch (WeatherApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error getting weather by location: {}", location, e);
            throw new WeatherApiException("Error al consultar clima por ubicación", e);
        }
    }

    /**
     * Fallback para getWeatherByLocation
     */
    private OpenWeatherResponseDTO getWeatherByLocationFallback(String location, Throwable t) {
        log.warn("Using fallback for location: {}, reason={}", location, t.getMessage());
        
        // Retornar respuesta genérica sin coordenadas específicas
        return createFallbackResponse(0.0, 0.0);
    }

    /**
     * Obtiene coordenadas geográficas por nombre de ciudad usando Geocoding API
     */
    private Map<String, Double> getCoordinatesByLocation(String location) {
        try {
            WebClient geocodingClient = WebClient.builder()
                    .baseUrl(GEOCODING_BASE_URL)
                    .build();
            
            List<Map<String, Object>> response = geocodingClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/direct")
                            .queryParam("q", location)
                            .queryParam("limit", 1)
                            .queryParam("appid", weatherApiProperties.getKey())
                            .build())
                    .retrieve()
                    .bodyToMono(List.class)
                    .timeout(Duration.ofMillis(weatherApiProperties.getTimeout()))
                    .block();
            
            if (response != null && !response.isEmpty()) {
                Map<String, Object> firstResult = response.get(0);
                Double lat = ((Number) firstResult.get("lat")).doubleValue();
                Double lon = ((Number) firstResult.get("lon")).doubleValue();
                
                log.info("Coordinates found for {}: lat={}, lon={}", location, lat, lon);
                return Map.of("lat", lat, "lon", lon);
            }
            
            log.warn("No coordinates found for location: {}", location);
            return null;
            
        } catch (Exception e) {
            log.error("Error getting coordinates for location: {}", location, e);
            return null;
        }
    }

    /**
     * Crea una respuesta genérica de fallback
     */
    private OpenWeatherResponseDTO createFallbackResponse(Double latitude, Double longitude) {
        OpenWeatherResponseDTO fallback = new OpenWeatherResponseDTO();
        fallback.setLat(latitude);
        fallback.setLon(longitude);
        fallback.setTimezone("Unknown");
        
        // Crear datos actuales genéricos
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(0.0);
        current.setFeelsLike(0.0);
        current.setHumidity(0);
        current.setClouds(0);
        current.setWindSpeed(0.0);
        
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(0);
        weather.setMain("Unavailable");
        weather.setDescription("Información del clima no disponible");
        current.setWeather(List.of(weather));
        
        fallback.setCurrent(current);
        
        // Crear datos diarios genéricos
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setPop(0.0);
        daily.setWeather(List.of(weather));
        
        fallback.setDaily(List.of(daily));
        
        return fallback;
    }

    /**
     * Health check del cliente
     */
    public boolean isHealthy() {
        try {
            // Probar con coordenadas de San José, CR
            getWeatherByCoordinates(9.9281, -84.0907);
            return true;
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }
}
