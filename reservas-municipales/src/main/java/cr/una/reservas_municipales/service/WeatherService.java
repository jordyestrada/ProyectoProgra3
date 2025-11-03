package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.client.WeatherApiClient;
import cr.una.reservas_municipales.dto.OpenWeatherResponseDTO;
import cr.una.reservas_municipales.dto.WeatherDTO;
import cr.una.reservas_municipales.exception.IndoorSpaceException;
import cr.una.reservas_municipales.exception.SpaceNotFoundException;
import cr.una.reservas_municipales.exception.WeatherApiException;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherApiClient weatherApiClient;
    private final SpaceRepository spaceRepository;

    @Cacheable(value = "weatherCache", key = "'space_' + #spaceId")
    public WeatherDTO getWeatherForSpace(UUID spaceId) {
        log.info("Fetching weather for space {}", spaceId);

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("Espacio no encontrado con ID: " + spaceId));

        if (!space.isOutdoor()) {
            log.warn("Attempting to get weather for indoor space: {}", spaceId);
            throw new IndoorSpaceException(
                    "El espacio '" + space.getName() + "' es interior y no requiere información del clima");
        }

        double latitude = 9.9281;
        double longitude = -84.0907;

        if (space.getLocation() != null && space.getLocation().contains(",")) {
            try {
                String[] parts = space.getLocation().split(",");
                if (parts.length == 2) {
                    latitude = Double.parseDouble(parts[0].trim());
                    longitude = Double.parseDouble(parts[1].trim());
                }
            } catch (NumberFormatException e) {
                log.warn("Could not parse coordinates from location: {}, using default", space.getLocation());
            }
        }

        try {
            OpenWeatherResponseDTO weatherData = weatherApiClient.getWeatherByCoordinates(latitude, longitude);

            return buildWeatherDTO(weatherData, space.getName(), "API");

        } catch (WeatherApiException e) {
            log.error("Error fetching weather for space {}", spaceId, e);

            return buildFallbackWeatherDTO(space.getName(), latitude, longitude);
        }
    }

    @Cacheable(value = "weatherCache", key = "'location_' + #location")
    public WeatherDTO getWeatherByLocation(String location) {
        log.info("Fetching weather for location: {}", location);

        if (location == null || location.trim().isEmpty()) {
            throw new WeatherApiException("La ubicación no puede estar vacía");
        }

        try {
            OpenWeatherResponseDTO weatherData = weatherApiClient.getWeatherByLocation(location);
            return buildWeatherDTO(weatherData, location, "API");

        } catch (WeatherApiException e) {
            log.error("Error fetching weather for location {}", location, e);
            return buildFallbackWeatherDTO(location, 0.0, 0.0);
        }
    }

    private WeatherDTO buildWeatherDTO(OpenWeatherResponseDTO weatherData, String location, String dataSource) {
        OpenWeatherResponseDTO.Current current = weatherData.getCurrent();
        OpenWeatherResponseDTO.Daily today = weatherData.getDaily() != null && !weatherData.getDaily().isEmpty()
                ? weatherData.getDaily().get(0)
                : null;

        Double temperature = current.getTemp();
        Double feelsLike = current.getFeelsLike();
        String description = current.getWeather() != null && !current.getWeather().isEmpty()
                ? current.getWeather().get(0).getDescription()
                : "No disponible";
        Integer humidity = current.getHumidity();
        Double windSpeed = current.getWindSpeed();
        Integer cloudiness = current.getClouds();
        Double rainProbability = today != null ? today.getPop() * 100 : 0.0;

        boolean isOutdoorFriendly = determineOutdoorFriendly(temperature, rainProbability, windSpeed);

        String recommendation = generateRecommendation(
                isOutdoorFriendly, temperature, rainProbability, windSpeed);

        return WeatherDTO.builder()
                .location(location)
                .temperature(temperature)
                .feelsLike(feelsLike)
                .description(description)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .cloudiness(cloudiness)
                .rainProbability(rainProbability)
                .isOutdoorFriendly(isOutdoorFriendly)
                .recommendation(recommendation)
                .dataSource(dataSource)
                .fetchedAt(OffsetDateTime.now())
                .latitude(weatherData.getLat())
                .longitude(weatherData.getLon())
                .build();
    }

    private WeatherDTO buildFallbackWeatherDTO(String location, Double lat, Double lon) {
        return WeatherDTO.builder()
                .location(location)
                .temperature(0.0)
                .feelsLike(0.0)
                .description("Información del clima no disponible")
                .humidity(0)
                .windSpeed(0.0)
                .cloudiness(0)
                .rainProbability(0.0)
                .isOutdoorFriendly(false)
                .recommendation("No se pudo obtener información del clima. Por favor, intente más tarde.")
                .dataSource("FALLBACK")
                .fetchedAt(OffsetDateTime.now())
                .latitude(lat)
                .longitude(lon)
                .build();
    }

    private boolean determineOutdoorFriendly(Double temperature, Double rainProbability, Double windSpeed) {
        if (temperature == null || rainProbability == null || windSpeed == null) {
            return false;
        }

        boolean tempOk = temperature >= 15 && temperature <= 30;
        boolean rainOk = rainProbability < 30;
        boolean windOk = windSpeed < 10;

        return tempOk && rainOk && windOk;
    }

    private String generateRecommendation(
            boolean isOutdoorFriendly,
            Double temperature,
            Double rainProbability,
            Double windSpeed) {

        if (isOutdoorFriendly) {
            return "Condiciones ideales para actividades al aire libre. ¡Disfruta tu reserva!";
        }

        StringBuilder recommendation = new StringBuilder("Se recomienda reprogramar la reserva:");

        if (temperature != null) {
            if (temperature < 15) {
                recommendation.append(" Temperatura baja (").append(String.format("%.1f", temperature)).append("°C).");
            } else if (temperature > 30) {
                recommendation.append(" Temperatura alta (").append(String.format("%.1f", temperature)).append("°C).");
            }
        }

        if (rainProbability != null && rainProbability >= 30) {
            recommendation.append(" Alta probabilidad de lluvia (")
                    .append(String.format("%.0f", rainProbability)).append("%).");
        }

        if (windSpeed != null && windSpeed >= 10) {
            recommendation.append(" Vientos fuertes (")
                    .append(String.format("%.1f", windSpeed)).append(" m/s).");
        }

        return recommendation.toString();
    }

    public boolean isHealthy() {
        try {
            return weatherApiClient.isHealthy();
        } catch (Exception e) {
            log.error("Service health check failed", e);
            return false;
        }
    }
}
