package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.SimpleDashboardDTO;
import cr.una.reservas_municipales.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {
    
    private final MetricsService metricsService;
    
    /**
     * GET /api/admin/dashboard
     * Obtiene todas las métricas del dashboard
     * Solo accesible para ADMIN y SUPERVISOR
     * 
     * @return SimpleDashboardDTO con métricas generales, estado, ingresos y top espacios
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<SimpleDashboardDTO> getDashboard() {
        log.info("Solicitud de dashboard recibida");
        SimpleDashboardDTO dashboard = metricsService.getSimpleDashboard();
        return ResponseEntity.ok(dashboard);
    }
}
