package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.*;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;

    /**
     * Dashboard completo - cacheable por 10 minutos
     */
    @Cacheable(value = "dashboardMetrics", key = "'simple-dashboard'")
    public SimpleDashboardDTO getSimpleDashboard() {
        log.info("Calculando métricas del dashboard...");
        
        SimpleDashboardDTO dashboard = new SimpleDashboardDTO();
        
        dashboard.setGeneralMetrics(calculateGeneralMetrics());
        dashboard.setReservationsByStatus(calculateReservationsByStatus());
        dashboard.setRevenueMetrics(calculateRevenueMetrics());
        dashboard.setTopSpaces(calculateTopSpaces(5)); // Top 5
        dashboard.setTemporalMetrics(calculateTemporalMetrics());
        
        log.info("Dashboard calculado exitosamente");
        return dashboard;
    }

    /**
     * Métricas generales usando solo ORM
     */
    private GeneralMetricsDTO calculateGeneralMetrics() {
        // Conteos simples usando métodos heredados de JpaRepository
        long totalReservations = reservationRepository.count();
        long totalSpaces = spaceRepository.count();
        long totalUsers = userRepository.count();
        
        // Contar reservas activas (CONFIRMED + PENDING)
        List<String> activeStatuses = Arrays.asList("CONFIRMED", "PENDING");
        long activeReservations = reservationRepository.countByStatusIn(activeStatuses);
        
        log.debug("Métricas generales - Reservas: {}, Espacios: {}, Usuarios: {}, Activas: {}", 
            totalReservations, totalSpaces, totalUsers, activeReservations);
        
        return new GeneralMetricsDTO(
            totalReservations,
            totalSpaces,
            totalUsers,
            activeReservations
        );
    }
    
    /**
     * Distribución por estado - ORM + conteo directo
     */
    private Map<String, Long> calculateReservationsByStatus() {
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("CONFIRMED", reservationRepository.countByStatus("CONFIRMED"));
        statusCount.put("PENDING", reservationRepository.countByStatus("PENDING"));
        statusCount.put("CANCELLED", reservationRepository.countByStatus("CANCELLED"));
        statusCount.put("COMPLETED", reservationRepository.countByStatus("COMPLETED"));
        
        log.debug("Distribución por estado: {}", statusCount);
        return statusCount;
    }
    
    /**
     * Métricas de ingresos - ORM + cálculos manuales
     */
    private RevenueMetricsDTO calculateRevenueMetrics() {
        // Obtener fecha del mes actual y anterior
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);
        
        OffsetDateTime currentMonthStart = currentMonth.atDay(1)
            .atStartOfDay()
            .atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime currentMonthEnd = currentMonth.atEndOfMonth()
            .atTime(23, 59, 59)
            .atOffset(OffsetDateTime.now().getOffset());
        
        OffsetDateTime lastMonthStart = lastMonth.atDay(1)
            .atStartOfDay()
            .atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime lastMonthEnd = lastMonth.atEndOfMonth()
            .atTime(23, 59, 59)
            .atOffset(OffsetDateTime.now().getOffset());
        
        // Traer reservas de ambos meses usando método derivado
        List<Reservation> currentMonthReservations = 
            reservationRepository.findByCreatedAtBetween(currentMonthStart, currentMonthEnd);
        
        List<Reservation> lastMonthReservations = 
            reservationRepository.findByCreatedAtBetween(lastMonthStart, lastMonthEnd);
        
        // Calcular ingresos con Streams (solo CONFIRMED y COMPLETED)
        double currentRevenue = currentMonthReservations.stream()
            .filter(r -> r.getStatus().equals("CONFIRMED") || r.getStatus().equals("COMPLETED"))
            .map(Reservation::getTotalAmount)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        
        double lastRevenue = lastMonthReservations.stream()
            .filter(r -> r.getStatus().equals("CONFIRMED") || r.getStatus().equals("COMPLETED"))
            .map(Reservation::getTotalAmount)
            .filter(Objects::nonNull)
            .mapToDouble(BigDecimal::doubleValue)
            .sum();
        
        // Calcular porcentaje de cambio
        double percentageChange = 0.0;
        if (lastRevenue > 0) {
            percentageChange = ((currentRevenue - lastRevenue) / lastRevenue) * 100;
        } else if (currentRevenue > 0) {
            percentageChange = 100.0; // Si el mes anterior fue 0 y ahora hay ingresos
        }
        
        // Redondear a 2 decimales
        percentageChange = Math.round(percentageChange * 100.0) / 100.0;
        
        log.debug("Ingresos - Actual: ${}, Anterior: ${}, Cambio: {}%", 
            currentRevenue, lastRevenue, percentageChange);
        
        return new RevenueMetricsDTO(currentRevenue, lastRevenue, percentageChange);
    }
    
    /**
     * Top espacios más reservados - ORM + procesamiento complejo
     */
    private List<TopSpaceDTO> calculateTopSpaces(int limit) {
        // Traer todos los espacios
        List<Space> allSpaces = spaceRepository.findAll();
        
        // Para cada espacio, calcular métricas
        List<TopSpaceDTO> topSpaces = allSpaces.stream()
            .map(space -> {
                // Traer reservas del espacio usando método derivado
                List<Reservation> spaceReservations = 
                    reservationRepository.findBySpaceId(space.getSpaceId());
                
                // Contar reservas
                long count = spaceReservations.size();
                
                // Calcular ingresos totales (solo CONFIRMED y COMPLETED)
                double revenue = spaceReservations.stream()
                    .filter(r -> r.getStatus().equals("CONFIRMED") || 
                                 r.getStatus().equals("COMPLETED"))
                    .map(Reservation::getTotalAmount)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();
                
                return new TopSpaceDTO(
                    space.getSpaceId(),
                    space.getName(),
                    count,
                    revenue
                );
            })
            // Ordenar por cantidad de reservas (descendente)
            .sorted(Comparator.comparingLong(TopSpaceDTO::getReservationCount).reversed())
            // Tomar solo los primeros N
            .limit(limit)
            .collect(Collectors.toList());
        
        log.debug("Top {} espacios calculados", topSpaces.size());
        return topSpaces;
    }
    
    /**
     * Métricas temporales - tendencias de uso por periodo
     */
    private TemporalMetricsDTO calculateTemporalMetrics() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // 1. Reservas de hoy (desde medianoche)
        OffsetDateTime todayStart = now.toLocalDate().atStartOfDay()
                                       .atOffset(now.getOffset());
        long today = reservationRepository.countByCreatedAtGreaterThanEqual(todayStart);
        
        // 2. Reservas de esta semana (últimos 7 días)
        OffsetDateTime weekStart = now.minusDays(7);
        long thisWeek = reservationRepository.countByCreatedAtGreaterThanEqual(weekStart);
        
        // 3. Reservas de este mes (últimos 30 días)
        OffsetDateTime monthStart = now.minusDays(30);
        long thisMonth = reservationRepository.countByCreatedAtGreaterThanEqual(monthStart);
        
        // 4. Obtener todas las reservas del último mes para análisis por día/hora
        List<Reservation> monthReservations = 
            reservationRepository.findByStartsAtBetween(monthStart, now.plusDays(30));
        
        // 5. Agrupar por día de la semana
        Map<String, Long> byDayOfWeek = monthReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStartsAt().getDayOfWeek().toString(),
                Collectors.counting()
            ));
        
        // 6. Agrupar por hora del día (0-23)
        Map<Integer, Long> byHour = monthReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStartsAt().getHour(),
                Collectors.counting()
            ));
        
        // 7. Encontrar día más popular
        String mostPopularDay = byDayOfWeek.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        // 8. Encontrar hora más popular
        Integer mostPopularHour = byHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(0);
        
        log.debug("Métricas temporales - Hoy: {}, Semana: {}, Mes: {}, Día popular: {}, Hora popular: {}h",
            today, thisWeek, thisMonth, mostPopularDay, mostPopularHour);
        
        return new TemporalMetricsDTO(
            today,
            thisWeek,
            thisMonth,
            byDayOfWeek,
            byHour,
            mostPopularDay,
            mostPopularHour
        );
    }
}
