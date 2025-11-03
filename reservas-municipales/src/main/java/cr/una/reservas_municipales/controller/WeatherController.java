package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.WeatherDTO;
import cr.una.reservas_municipales.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/space/{spaceId}")
    public ResponseEntity<WeatherDTO> getWeatherForSpace(@PathVariable UUID spaceId) {
        log.info("GET /api/weather/space/{}", spaceId);
        
        WeatherDTO weather = weatherService.getWeatherForSpace(spaceId);
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/location")
    public ResponseEntity<WeatherDTO> getWeatherByLocation(
            @RequestParam(name = "location") String location) {
        log.info("GET /api/weather/location?location={}", location);
        
        WeatherDTO weather = weatherService.getWeatherByLocation(location);
        return ResponseEntity.ok(weather);
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("GET /api/weather/health");
        
        boolean isHealthy = weatherService.isHealthy();
        
        return ResponseEntity.ok(Map.of(
                "status", isHealthy ? "UP" : "DOWN",
                "service", "Weather API Integration",
                "healthy", isHealthy,
                "timestamp", java.time.OffsetDateTime.now()
        ));
    }
}
