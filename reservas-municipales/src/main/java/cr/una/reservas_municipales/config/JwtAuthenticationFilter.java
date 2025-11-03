package cr.una.reservas_municipales.config;

import cr.una.reservas_municipales.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);

        if (jwtService.validateToken(token)) {
            String username = jwtService.getUsernameFromToken(token);
            
            io.jsonwebtoken.Claims claims = jwtService.getClaimsFromToken(token);
            Object rawAuth = claims.get("authorities");
            if (rawAuth == null) rawAuth = claims.get("roles");

            java.util.List<String> roles;
            if (rawAuth == null) {
                roles = java.util.List.of();
            } else if (rawAuth instanceof String s) {
                roles = java.util.Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(r -> !r.isEmpty())
                        .toList();
            } else if (rawAuth instanceof java.util.Collection<?> c) {
                roles = c.stream().map(Object::toString).toList();
            } else {
                roles = java.util.List.of();
            }

            var authorities = roles.stream()
                    .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}