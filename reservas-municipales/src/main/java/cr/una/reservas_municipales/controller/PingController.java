package cr.una.reservas_municipales.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/api/ping")
    public Map<String, Object> authenticatedPing() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "authenticated pong");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        return response;
    }
}
