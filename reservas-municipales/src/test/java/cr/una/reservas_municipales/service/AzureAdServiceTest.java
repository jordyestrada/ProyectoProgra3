package cr.una.reservas_municipales.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AzureAdServiceTest {

    private AzureAdService service;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;

    private static final String GRAPH_ME = "https://graph.microsoft.com/v1.0/me";

    @BeforeEach
    void setup() throws Exception {
        service = new AzureAdService();
        // reflect the internal RestTemplate instance so we can bind MockRestServiceServer
        Field f = AzureAdService.class.getDeclaredField("restTemplate");
        f.setAccessible(true);
        restTemplate = (RestTemplate) f.get(service);
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    void validateAzureToken_ok_returnsTrue_andSetsHeaders() {
        server.expect(once(), requestTo(GRAPH_ME))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer abc123"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        boolean result = service.validateAzureToken("abc123");

        assertTrue(result);
        server.verify();
    }

    @Test
    void validateAzureToken_nonOk_returnsFalse() {
        server.expect(once(), requestTo(GRAPH_ME))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON));

        boolean result = service.validateAzureToken("bad-token");
        assertFalse(result);
        server.verify();
    }

    @Test
    void validateAzureToken_exception_returnsFalse() {
        server.expect(once(), requestTo(GRAPH_ME))
                .andRespond(request -> { throw new RuntimeException("boom"); });

        boolean result = service.validateAzureToken("any");
        assertFalse(result);
        server.verify();
    }

    @Test
    void getUserInfoFromToken_ok_parsesAllFields() {
        String json = "{" +
                "\"mail\":\"user@example.com\"," +
                "\"displayName\":\"John Doe\"," +
                "\"givenName\":\"John\"," +
                "\"surname\":\"Doe\"," +
                "\"id\":\"abc-123\"" +
                "}";

        server.expect(once(), requestTo(GRAPH_ME))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer tok"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        AzureAdService.AzureUserInfo info = service.getUserInfoFromToken("tok");

        assertNotNull(info);
        assertEquals("user@example.com", info.getEmail());
        assertEquals("John Doe", info.getDisplayName());
        assertEquals("John", info.getGivenName());
        assertEquals("Doe", info.getSurname());
        assertEquals("abc-123", info.getId());
        server.verify();
    }

    @Test
    void getUserInfoFromToken_nonOk_returnsNull() {
        server.expect(once(), requestTo(GRAPH_ME))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        AzureAdService.AzureUserInfo info = service.getUserInfoFromToken("tok");
        assertNull(info);
        server.verify();
    }

    @Test
    void getUserInfoFromToken_malformedBody_returnsNull() {
        // Missing required fields will trigger a NullPointer when asText() is called
        String jsonMissingFields = "{\"displayName\":\"Only Name\"}";

        server.expect(once(), requestTo(GRAPH_ME))
                .andRespond(withSuccess(jsonMissingFields, MediaType.APPLICATION_JSON));

        AzureAdService.AzureUserInfo info = service.getUserInfoFromToken("tok");
        assertNull(info);
        server.verify();
    }
}
