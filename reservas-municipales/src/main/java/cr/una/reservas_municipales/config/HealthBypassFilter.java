package cr.una.reservas_municipales.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("disabled")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HealthBypassFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse resp) {
            String path = req.getRequestURI();
            if ("/actuator/health".equals(path) || "/ping".equals(path)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"status\":\"UP\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
