package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.*;
import cr.una.reservas_municipales.service.MetricsService;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsService metricsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private SimpleDashboardDTO dashboardDTO;

    @BeforeEach
    void setUp() {
        dashboardDTO = new SimpleDashboardDTO();
        
        // General metrics
        GeneralMetricsDTO generalMetrics = new GeneralMetricsDTO();
        generalMetrics.setTotalReservations(150);
        generalMetrics.setTotalUsers(75);
        generalMetrics.setTotalSpaces(12);
        generalMetrics.setActiveReservations(45);
        dashboardDTO.setGeneralMetrics(generalMetrics);
        
        // Status breakdown
        Map<String, Long> statusBreakdown = new HashMap<>();
        statusBreakdown.put("PENDING", 20L);
        statusBreakdown.put("CONFIRMED", 25L);
        statusBreakdown.put("COMPLETED", 100L);
        statusBreakdown.put("CANCELLED", 5L);
        dashboardDTO.setReservationsByStatus(statusBreakdown);
        
        // Revenue
        RevenueMetricsDTO revenueMetrics = new RevenueMetricsDTO();
        revenueMetrics.setCurrentMonthRevenue(250000.0);
        revenueMetrics.setLastMonthRevenue(200000.0);
        revenueMetrics.setPercentageChange(25.0);
        dashboardDTO.setRevenueMetrics(revenueMetrics);
        
        // Top spaces
        TopSpaceDTO topSpace = new TopSpaceDTO();
        topSpace.setSpaceId(UUID.randomUUID());
        topSpace.setSpaceName("Cancha de Fútbol");
        topSpace.setReservationCount(45);
        topSpace.setTotalRevenue(500000.0);
        dashboardDTO.setTopSpaces(Arrays.asList(topSpace));
    }

    @Test
    void testGetDashboard_Success() throws Exception {
        when(metricsService.getSimpleDashboard()).thenReturn(dashboardDTO);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generalMetrics.totalReservations").value(150))
                .andExpect(jsonPath("$.generalMetrics.totalUsers").value(75))
                .andExpect(jsonPath("$.generalMetrics.totalSpaces").value(12))
                .andExpect(jsonPath("$.generalMetrics.activeReservations").value(45))
                .andExpect(jsonPath("$.reservationsByStatus.PENDING").value(20))
                .andExpect(jsonPath("$.reservationsByStatus.CONFIRMED").value(25))
                .andExpect(jsonPath("$.reservationsByStatus.COMPLETED").value(100))
                .andExpect(jsonPath("$.revenueMetrics.currentMonthRevenue").value(250000.0))
                .andExpect(jsonPath("$.topSpaces[0].spaceName").value("Cancha de Fútbol"))
                .andExpect(jsonPath("$.topSpaces[0].reservationCount").value(45));

        verify(metricsService, times(1)).getSimpleDashboard();
    }

    @Test
    void testGetDashboard_EmptyData() throws Exception {
        SimpleDashboardDTO emptyDashboard = new SimpleDashboardDTO();
        emptyDashboard.setGeneralMetrics(new GeneralMetricsDTO());
        emptyDashboard.setReservationsByStatus(new HashMap<>());
        emptyDashboard.setRevenueMetrics(new RevenueMetricsDTO());
        emptyDashboard.setTopSpaces(new ArrayList<>());

        when(metricsService.getSimpleDashboard()).thenReturn(emptyDashboard);

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generalMetrics").exists())
                .andExpect(jsonPath("$.reservationsByStatus").exists())
                .andExpect(jsonPath("$.revenueMetrics").exists())
                .andExpect(jsonPath("$.topSpaces").exists());

        verify(metricsService, times(1)).getSimpleDashboard();
    }

    @Test
    void testGetDashboard_ServiceException() throws Exception {
        when(metricsService.getSimpleDashboard()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isInternalServerError());

        verify(metricsService, times(1)).getSimpleDashboard();
    }
}
