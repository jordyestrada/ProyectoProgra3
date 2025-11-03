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

    @Cacheable(value = "dashboardMetrics", key = "'simple-dashboard'")
    public SimpleDashboardDTO getSimpleDashboard() {
        log.info("Calculando métricas del dashboard...");
        
        SimpleDashboardDTO dashboard = new SimpleDashboardDTO();
        
        dashboard.setGeneralMetrics(calculateGeneralMetrics());
        dashboard.setReservationsByStatus(calculateReservationsByStatus());
        dashboard.setRevenueMetrics(calculateRevenueMetrics());
        dashboard.setTopSpaces(calculateTopSpaces(5)); 
        dashboard.setTemporalMetrics(calculateTemporalMetrics());
        
        log.info("Dashboard calculado exitosamente");
        return dashboard;
    }

    
    private GeneralMetricsDTO calculateGeneralMetrics() {
        
        long totalReservations = reservationRepository.count();
        long totalSpaces = spaceRepository.count();
        long totalUsers = userRepository.count();
        
        
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
    
    private Map<String, Long> calculateReservationsByStatus() {
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("CONFIRMED", reservationRepository.countByStatus("CONFIRMED"));
        statusCount.put("PENDING", reservationRepository.countByStatus("PENDING"));
        statusCount.put("CANCELLED", reservationRepository.countByStatus("CANCELLED"));
        statusCount.put("COMPLETED", reservationRepository.countByStatus("COMPLETED"));
        
        log.debug("Distribución por estado: {}", statusCount);
        return statusCount;
    }
    
    private RevenueMetricsDTO calculateRevenueMetrics() {
        
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
        
        
        List<Reservation> currentMonthReservations = 
            reservationRepository.findByCreatedAtBetween(currentMonthStart, currentMonthEnd);
        
        List<Reservation> lastMonthReservations = 
            reservationRepository.findByCreatedAtBetween(lastMonthStart, lastMonthEnd);
        
        
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
        
        
        double percentageChange = 0.0;
        if (lastRevenue > 0) {
            percentageChange = ((currentRevenue - lastRevenue) / lastRevenue) * 100;
        } else if (currentRevenue > 0) {
            percentageChange = 100.0; 
        }
        
        
        percentageChange = Math.round(percentageChange * 100.0) / 100.0;
        
        log.debug("Ingresos - Actual: ${}, Anterior: ${}, Cambio: {}%", 
            currentRevenue, lastRevenue, percentageChange);
        
        return new RevenueMetricsDTO(currentRevenue, lastRevenue, percentageChange);
    }
    
    private List<TopSpaceDTO> calculateTopSpaces(int limit) {
        
        List<Space> allSpaces = spaceRepository.findAll();
        
        
        List<TopSpaceDTO> topSpaces = allSpaces.stream()
            .map(space -> {
                
                List<Reservation> spaceReservations = 
                    reservationRepository.findBySpaceId(space.getSpaceId());
                
                
                long count = spaceReservations.size();
                
                
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
            
            .sorted(Comparator.comparingLong(TopSpaceDTO::getReservationCount).reversed())
            
            .limit(limit)
            .collect(Collectors.toList());
        
        log.debug("Top {} espacios calculados", topSpaces.size());
        return topSpaces;
    }
    
    private TemporalMetricsDTO calculateTemporalMetrics() {
        OffsetDateTime now = OffsetDateTime.now();
        
        
        OffsetDateTime todayStart = now.toLocalDate().atStartOfDay()
                                       .atOffset(now.getOffset());
        long today = reservationRepository.countByCreatedAtGreaterThanEqual(todayStart);
        
        
        OffsetDateTime weekStart = now.minusDays(7);
        long thisWeek = reservationRepository.countByCreatedAtGreaterThanEqual(weekStart);
        
        
        OffsetDateTime monthStart = now.minusDays(30);
        long thisMonth = reservationRepository.countByCreatedAtGreaterThanEqual(monthStart);
        
        
        List<Reservation> monthReservations = 
            reservationRepository.findByStartsAtBetween(monthStart, now.plusDays(30));
        
        
        Map<String, Long> byDayOfWeek = monthReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStartsAt().getDayOfWeek().toString(),
                Collectors.counting()
            ));
        
        
        Map<Integer, Long> byHour = monthReservations.stream()
            .collect(Collectors.groupingBy(
                r -> r.getStartsAt().getHour(),
                Collectors.counting()
            ));
        
        
        String mostPopularDay = byDayOfWeek.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        
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
