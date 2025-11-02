package cr.una.reservas_municipales.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para HealthBypassFilter
 * Nota: Este filtro está deshabilitado por defecto (@Profile("disabled"))
 */
@ExtendWith(MockitoExtension.class)
class HealthBypassFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private HealthBypassFilter healthBypassFilter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() {
        healthBypassFilter = new HealthBypassFilter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    void testDoFilter_HealthEndpoint() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(any(), any());
        
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("\"status\":\"UP\""));
    }

    @Test
    void testDoFilter_PingEndpoint() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ping");
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(filterChain, never()).doFilter(any(), any());
        
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("\"status\":\"UP\""));
    }

    @Test
    void testDoFilter_OtherEndpoint() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/reservations");

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setContentType(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testDoFilter_NonHttpRequest() throws ServletException, IOException {
        // Arrange
        ServletRequest nonHttpRequest = mock(ServletRequest.class);
        ServletResponse nonHttpResponse = mock(ServletResponse.class);

        // Act
        healthBypassFilter.doFilter(nonHttpRequest, nonHttpResponse, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(nonHttpRequest, nonHttpResponse);
    }

    @Test
    void testDoFilter_HealthEndpointJsonResponse() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert
        printWriter.flush();
        String output = stringWriter.toString();
        assertEquals("{\"status\":\"UP\"}", output);
    }

    @Test
    void testDoFilter_PingEndpointJsonResponse() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/ping");
        when(response.getWriter()).thenReturn(printWriter);

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert
        printWriter.flush();
        String output = stringWriter.toString();
        assertEquals("{\"status\":\"UP\"}", output);
    }

    @Test
    void testDoFilter_CaseSensitiveUri() throws ServletException, IOException {
        // Arrange - URI con diferente capitalización
        when(request.getRequestURI()).thenReturn("/Actuator/Health");

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert - No debe bypassear por ser case-sensitive
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_HealthSubpath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/actuator/health/liveness");

        // Act
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert - No debe bypassear
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testDoFilter_MultipleRequests() throws ServletException, IOException {
        // Test que el filtro puede manejar múltiples requests
        
        // Primera request - health
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(response.getWriter()).thenReturn(printWriter);
        healthBypassFilter.doFilter(request, response, filterChain);
        
        // Segunda request - ping
        reset(request, response, filterChain);
        when(request.getRequestURI()).thenReturn("/ping");
        when(response.getWriter()).thenReturn(printWriter);
        healthBypassFilter.doFilter(request, response, filterChain);
        
        // Tercera request - normal
        reset(request, response, filterChain);
        when(request.getRequestURI()).thenReturn("/api/users");
        healthBypassFilter.doFilter(request, response, filterChain);

        // Assert - La tercera debe pasar por el chain
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilterInstantiation() {
        // Assert
        assertNotNull(healthBypassFilter);
    }
}
