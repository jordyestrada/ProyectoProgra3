package cr.una.reservas_municipales.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración del WebClient para consumir la API del clima
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WeatherWebClientConfig {

    private final WeatherApiProperties weatherApiProperties;

    @Bean(name = "weatherWebClient")
    public WebClient weatherWebClient() {
        // Configurar timeout y opciones de conexión
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, weatherApiProperties.getTimeout())
                .responseTimeout(Duration.ofMillis(weatherApiProperties.getTimeout()))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(weatherApiProperties.getTimeout(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(weatherApiProperties.getTimeout(), TimeUnit.MILLISECONDS)));

        log.info("Configurando WebClient para Weather API con timeout de {}ms", weatherApiProperties.getTimeout());

        return WebClient.builder()
                .baseUrl(weatherApiProperties.getUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
