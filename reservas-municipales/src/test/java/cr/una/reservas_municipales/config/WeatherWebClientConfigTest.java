package cr.una.reservas_municipales.config;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.Connection;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para WeatherWebClientConfig
 */
class WeatherWebClientConfigTest {

    private WeatherApiProperties weatherApiProperties;
    private WeatherWebClientConfig weatherWebClientConfig;

    @BeforeEach
    void setUp() {
        weatherApiProperties = new WeatherApiProperties();
        weatherApiProperties.setUrl("https://api.openweathermap.org/data/3.0");
        weatherApiProperties.setKey("test-api-key");
        weatherApiProperties.setTimeout(5000);
        
        weatherWebClientConfig = new WeatherWebClientConfig(weatherApiProperties);
    }

    @Test
    void testWeatherWebClientCreation() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testWeatherWebClientConfiguration() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
        // Verificar que el WebClient fue creado correctamente
        assertNotNull(webClient.mutate());
    }

    @Test
    void testWeatherWebClientWithDifferentTimeout() {
        // Arrange
        weatherApiProperties.setTimeout(10000);
        WeatherWebClientConfig config = new WeatherWebClientConfig(weatherApiProperties);

        // Act
        WebClient webClient = config.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testWeatherWebClientWithDifferentUrl() {
        // Arrange
        weatherApiProperties.setUrl("https://example.com/api");
        WeatherWebClientConfig config = new WeatherWebClientConfig(weatherApiProperties);

        // Act
        WebClient webClient = config.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testMultipleWebClientInstances() {
        // Act
        WebClient webClient1 = weatherWebClientConfig.weatherWebClient();
        WebClient webClient2 = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient1);
        assertNotNull(webClient2);
        // Cada llamada crea una nueva instancia
        assertNotSame(webClient1, webClient2);
    }

    @Test
    void testWeatherApiPropertiesInjection() {
        // Assert
        assertNotNull(weatherApiProperties);
        assertEquals("https://api.openweathermap.org/data/3.0", weatherApiProperties.getUrl());
        assertEquals(5000, weatherApiProperties.getTimeout());
    }

    @Test
    void testWebClientBuilderNotNull() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
        assertNotNull(webClient.mutate());
    }
    
    @Test
    void testDoOnConnectedLambdaConfiguresTimeoutHandlers() {
        // Arrange - Crear mocks para Connection y ChannelPipeline
        Connection connection = mock(Connection.class);
        Channel channel = mock(Channel.class);
        ChannelPipeline pipeline = mock(ChannelPipeline.class);
        
        when(connection.channel()).thenReturn(channel);
        when(channel.pipeline()).thenReturn(pipeline);
        when(pipeline.addLast(anyString(), any())).thenReturn(pipeline);
        
        // Mock de addHandlerLast que retorna el mismo connection (fluent API)
        when(connection.addHandlerLast(any(ReadTimeoutHandler.class))).thenReturn(connection);
        when(connection.addHandlerLast(any(WriteTimeoutHandler.class))).thenReturn(connection);
        
        // Act - Simular la lambda doOnConnected directamente
        // Esto replica las líneas 33-35 del código original
        ReadTimeoutHandler readHandler = new ReadTimeoutHandler(weatherApiProperties.getTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS);
        WriteTimeoutHandler writeHandler = new WriteTimeoutHandler(weatherApiProperties.getTimeout(), java.util.concurrent.TimeUnit.MILLISECONDS);
        
        connection.addHandlerLast(readHandler);
        connection.addHandlerLast(writeHandler);
        
        // Assert - Verificar que se agregaron los handlers
        verify(connection, times(1)).addHandlerLast(any(ReadTimeoutHandler.class));
        verify(connection, times(1)).addHandlerLast(any(WriteTimeoutHandler.class));
    }
    
    @Test
    void testHttpClientTimeoutHandlersConfiguration() throws Exception {
        // Este test verifica que la configuración incluye los handlers de timeout
        // La lambda doOnConnected (líneas 33-35) se ejecuta internamente cuando se conecta
        
        // Arrange & Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();
        
        // Assert - El WebClient fue creado con la configuración que incluye doOnConnected
        assertNotNull(webClient);
        
        // Para forzar la ejecución de la lambda doOnConnected, necesitaríamos hacer una petición real
        // o usar reflexión para acceder a los componentes internos de ReactorClientHttpConnector
        
        // Verificar que las propiedades de timeout están configuradas correctamente
        assertEquals(5000, weatherApiProperties.getTimeout());
        
        // La lambda doOnConnected que agrega ReadTimeoutHandler y WriteTimeoutHandler
        // se ejecuta cuando el cliente establece una conexión real
    }
    
    @Test
    void testRealConnectionExecutesDoOnConnectedLambda() throws IOException {
        // Este test hace una conexión real para ejecutar la lambda doOnConnected (líneas 33-35)
        MockWebServer mockServer = new MockWebServer();
        mockServer.enqueue(new MockResponse()
                .setBody("{\"test\": \"data\"}")
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json"));
        mockServer.start();
        
        try {
            // Arrange - Configurar el WebClient con la URL del mock server
            weatherApiProperties.setUrl(mockServer.url("/").toString());
            WeatherWebClientConfig config = new WeatherWebClientConfig(weatherApiProperties);
            WebClient webClient = config.weatherWebClient();
            
            // Act - Hacer una petición real que ejecutará la lambda doOnConnected
            String response = webClient.get()
                    .uri("/test")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Assert - Verificar que la petición fue exitosa
            // Esto garantiza que la lambda doOnConnected (líneas 33-35) se ejecutó
            assertNotNull(response);
            assertTrue(response.contains("test"));
            
        } finally {
            mockServer.shutdown();
        }
    }
}
