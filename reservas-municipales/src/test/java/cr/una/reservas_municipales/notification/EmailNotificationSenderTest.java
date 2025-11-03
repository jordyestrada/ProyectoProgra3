package cr.una.reservas_municipales.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EmailNotificationSender
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailNotificationSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationSender emailSender;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailSender, "from", "no-reply@test.com");
        ReflectionTestUtils.setField(emailSender, "adminCopy", "admin@test.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void testSendReservationCreatedEmail() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de fútbol");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("user@test.com")
            .data(data)
            .occurredAt(OffsetDateTime.now())
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendStatusChangedEmail() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Salón de eventos");
        data.put("startsAt", "2025-11-01 14:00");
        data.put("endsAt", "2025-11-01 16:00");
        data.put("oldStatus", "PENDING");
        data.put("newStatus", "CONFIRMED");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_STATUS_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendCancelledEmail() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de tenis");
        data.put("startsAt", "2025-11-02 09:00");
        data.put("endsAt", "2025-11-02 10:00");
        data.put("reason", "Mal tiempo");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CANCELLED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendQrValidatedEmail() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Gimnasio");
        data.put("startsAt", "2025-11-01 08:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendUserRoleChangedEmail() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "Juan Pérez");
        data.put("oldRole", "ROLE_USER");
        data.put("newRole", "ROLE_ADMIN");
        data.put("newRoleName", "Administrador");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSkipEmailWhenRecipientIsNull() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email(null)
            .build();

        emailSender.send(event);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSkipEmailWhenRecipientIsBlank() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("   ")
            .build();

        emailSender.send(event);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testSkipEmailWhenRecipientIsEmpty() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("")
            .build();

        emailSender.send(event);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testAdminCopyOnReservationCreated() {
        ReflectionTestUtils.setField(emailSender, "adminCopy", "admin@test.com");
        
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testNoAdminCopyOnOtherNotifications() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha");
        data.put("startsAt", "2025-11-01 10:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSubjectGeneration() {
        // Test indirectamente verificando que se envía un email con el tipo correcto
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Test");
        data.put("startsAt", "2025-11-01");
        data.put("endsAt", "2025-11-01");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testBodyContainsSpaceName() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha Municipal");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testEmailWithDefaultDataValues() {
        Map<String, Object> data = new HashMap<>();
        // No incluir spaceName, startsAt, endsAt para probar valores por defecto

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testRoleChangedWithAdminRole() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "Test User");
        data.put("oldRole", "ROLE_USER");
        data.put("newRole", "ROLE_ADMIN");
        data.put("newRoleName", "Administrador");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testRoleChangedWithSupervisorRole() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "Test User");
        data.put("oldRole", "ROLE_USER");
        data.put("newRole", "ROLE_SUPERVISOR");
        data.put("newRoleName", "Supervisor");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testRoleChangedWithUserRole() {
        Map<String, Object> data = new HashMap<>();
        data.put("userName", "Test User");
        data.put("oldRole", "ROLE_SUPERVISOR");
        data.put("newRole", "ROLE_USER");
        data.put("newRoleName", "Usuario");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        emailSender.send(event);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    // ========== TESTS FOR EXCEPTION HANDLING (Lines 48-50) ==========
    // Estos tests cubren específicamente el bloque catch (MessagingException ex)

    @Test
    void testMessagingExceptionThrownAndCaught_InvalidFromAddress() {
        // Crear un MimeMessage real que lance MessagingException
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        // Configurar un from con caracteres inválidos que cause MessagingException
        ReflectionTestUtils.setField(emailSender, "from", "invalid\nemail@test.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Test Space");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("user@test.com")
            .data(data)
            .build();

        // Debe lanzar RuntimeException envolviendo la MessagingException (línea 50)
        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void testMessagingExceptionThrownAndCaught_InvalidToAddress() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "test@test.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de fútbol");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("invalid\nemail")  // Email con salto de línea causa MessagingException
            .data(data)
            .build();

        // Línea 48-50: catch MessagingException, log error, throw RuntimeException
        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionThrownAndCaught_InvalidCcAddress() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "test@test.com");
        ReflectionTestUtils.setField(emailSender, "adminCopy", "invalid\ncc@email.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Salón");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("valid@test.com")
            .data(data)
            .build();

        // El adminCopy inválido causa MessagingException en setCc()
        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionLogsCorrectEmail() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "test\n@invalid.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Test");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("specific-user@test.com")
            .data(data)
            .build();

        // Verifica que se ejecuta la línea 49: log.error con el email del destinatario
        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email");
        
        // El email "specific-user@test.com" debe aparecer en el log (línea 49)
    }

    @Test
    void testMessagingExceptionForStatusChanged() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "bad\nformat@test.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Salón");
        data.put("startsAt", "2025-11-01 14:00");
        data.put("endsAt", "2025-11-01 16:00");
        data.put("oldStatus", "PENDING");
        data.put("newStatus", "CONFIRMED");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_STATUS_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionForCancelled() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "\n");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha");
        data.put("startsAt", "2025-11-02 09:00");
        data.put("endsAt", "2025-11-02 10:00");
        data.put("reason", "Mal tiempo");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CANCELLED)
            .email("user@test.com")
            .data(data)
            .build();

        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionForQrValidation() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "test@\ninvalid.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Gimnasio");
        data.put("startsAt", "2025-11-01 08:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("user@test.com")
            .data(data)
            .build();

        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionForRoleChange() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "@\ninvalid");

        Map<String, Object> data = new HashMap<>();
        data.put("userName", "Test User");
        data.put("oldRole", "ROLE_USER");
        data.put("newRole", "ROLE_ADMIN");
        data.put("newRoleName", "Administrador");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("user@test.com")
            .data(data)
            .build();

        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionWrappedInRuntimeException() {
        // Verifica que MessagingException se envuelve correctamente en RuntimeException
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "not\nvalid-email@test.com");

        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Test");
        data.put("startsAt", "2025-11-01 10:00");
        data.put("endsAt", "2025-11-01 12:00");

        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@example.com")
            .data(data)
            .build();

        // Verifica línea 50: throw new RuntimeException("Error sending email", ex);
        assertThatThrownBy(() -> emailSender.send(event))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Error sending email")
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testMessagingExceptionWithAllNotificationTypes() {
        Session session = Session.getInstance(new Properties());
        MimeMessage realMessage = new MimeMessage(session);
        
        when(mailSender.createMimeMessage()).thenReturn(realMessage);
        ReflectionTestUtils.setField(emailSender, "from", "bad\nemail@test.com");

        // Probar que todas las notificaciones pasan por el catch
        NotificationType[] types = {
            NotificationType.RESERVATION_CREATED,
            NotificationType.RESERVATION_STATUS_CHANGED,
            NotificationType.RESERVATION_CANCELLED,
            NotificationType.QR_VALIDATED,
            NotificationType.USER_ROLE_CHANGED
        };

        for (NotificationType type : types) {
            Map<String, Object> data = new HashMap<>();
            data.put("spaceName", "Test");
            data.put("startsAt", "2025-11-01 10:00");
            data.put("endsAt", "2025-11-01 12:00");
            data.put("oldStatus", "OLD");
            data.put("newStatus", "NEW");
            data.put("reason", "test");
            data.put("userName", "User");
            data.put("oldRole", "ROLE_USER");
            data.put("newRole", "ROLE_ADMIN");
            data.put("newRoleName", "Admin");

            NotificationEvent event = NotificationEvent.builder()
                .type(type)
                .email("user@test.com")
                .data(data)
                .build();

            assertThatThrownBy(() -> emailSender.send(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error sending email")
                .hasCauseInstanceOf(MessagingException.class);
        }
    }
}
