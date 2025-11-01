package cr.una.reservas_municipales.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AzureAdServiceTest {

    @InjectMocks
    private AzureAdService azureAdService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(azureAdService, "tenantId", "test-tenant-id");
        ReflectionTestUtils.setField(azureAdService, "clientId", "test-client-id");
    }

    @Test
    void testValidateAzureToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";

        // Act
        boolean result = azureAdService.validateAzureToken(invalidToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateAzureToken_NullToken() {
        // Act
        boolean result = azureAdService.validateAzureToken(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateAzureToken_EmptyToken() {
        // Act
        boolean result = azureAdService.validateAzureToken("");

        // Assert
        assertFalse(result);
    }

    @Test
    void testGetUserInfoFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid-token";

        // Act
        AzureAdService.AzureUserInfo result = azureAdService.getUserInfoFromToken(invalidToken);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetUserInfoFromToken_NullToken() {
        // Act
        AzureAdService.AzureUserInfo result = azureAdService.getUserInfoFromToken(null);

        // Assert
        assertNull(result);
    }
}
