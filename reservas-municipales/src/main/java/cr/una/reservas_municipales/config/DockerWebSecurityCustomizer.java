package cr.una.reservas_municipales.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
@Profile("disabled")
public class DockerWebSecurityCustomizer {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Completely bypass Spring Security filters for these paths in the docker profile
        return (web) -> web.ignoring().requestMatchers("/actuator/health", "/actuator/info", "/ping");
    }
}
