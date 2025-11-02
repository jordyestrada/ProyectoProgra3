package cr.una.reservas_municipales.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.from:${APP_NOTIFICATIONS_FROM:no-reply@localhost}}")
    private String from;

    @Value("${app.notifications.admin:${APP_NOTIFICATIONS_ADMIN:}}")
    private String adminCopy;

    @Override
    public void send(NotificationEvent e) {
        if (e.getEmail() == null || e.getEmail().isBlank()) {
            log.warn("Skipping email: missing recipient for {}", e.getType());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(e.getEmail());
            helper.setSubject(subject(e));
            helper.setText(body(e), true); // true = HTML
            
            // sin persistir, sin BD. CC opcional para creación
            if (!adminCopy.isBlank() && e.getType() == NotificationType.RESERVATION_CREATED) {
                helper.setCc(adminCopy);
            }
            
            mailSender.send(mimeMessage);
            log.info("Email sent: type={}, to={}", e.getType(), e.getEmail());
        } catch (MessagingException ex) {
            log.error("Failed to send email to {}: {}", e.getEmail(), ex.getMessage());
            throw new RuntimeException("Error sending email", ex);
        }
    }

    private String subject(NotificationEvent e) {
        return switch (e.getType()) {
            case RESERVATION_CREATED -> "Reserva creada";
            case RESERVATION_STATUS_CHANGED -> "Estado de tu reserva actualizado";
            case RESERVATION_CANCELLED -> "Reserva cancelada";
            case QR_VALIDATED -> "Asistencia confirmada";
            case USER_ROLE_CHANGED -> "Tu rol en el sistema ha sido actualizado";
        };
    }

    private String body(NotificationEvent e) {
        String space = String.valueOf(e.getData().getOrDefault("spaceName", "Espacio"));
        String starts = String.valueOf(e.getData().getOrDefault("startsAt", ""));
        String ends   = String.valueOf(e.getData().getOrDefault("endsAt", ""));
        
        return switch (e.getType()) {
            case RESERVATION_CREATED -> buildHtmlReservationCreated(space, starts, ends);
            case RESERVATION_STATUS_CHANGED -> buildHtmlStatusChanged(
                String.valueOf(e.getData().getOrDefault("oldStatus","?")),
                String.valueOf(e.getData().getOrDefault("newStatus","?")),
                space, starts, ends
            );
            case RESERVATION_CANCELLED -> buildHtmlCancelled(
                String.valueOf(e.getData().getOrDefault("reason","(sin motivo)")),
                space, starts, ends
            );
            case QR_VALIDATED -> buildHtmlQrValidated(space, starts);
            case USER_ROLE_CHANGED -> buildHtmlRoleChanged(
                String.valueOf(e.getData().getOrDefault("userName","Usuario")),
                String.valueOf(e.getData().getOrDefault("oldRole","?")),
                String.valueOf(e.getData().getOrDefault("newRole","?")),
                String.valueOf(e.getData().getOrDefault("newRoleName","Nuevo Rol"))
            );
        };
    }
    
    private String buildHtmlReservationCreated(String space, String starts, String ends) {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <meta name="supported-color-schemes" content="light dark">
    <title>Reserva Creada</title>
</head>
<body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#f5f7fa;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background:#f5f7fa;padding:24px 0;">
        <tr>
            <td align="center">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #e6e9ef;box-shadow:0 2px 8px rgba(0,0,0,0.04);">
                    <tr>
                        <td style="padding:32px 28px 16px 28px;background:linear-gradient(135deg,#2563eb 0%%,#1d4ed8 100%%);border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;line-height:32px;color:#ffffff;font-weight:600;">
                                ✅ Reserva Creada Exitosamente
                            </h1>
                            <p style="margin:8px 0 0 0;font-size:14px;line-height:20px;color:#e0e7ff;">
                                Tu reserva ha sido registrada en el sistema
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px;">
                            <p style="margin:0 0 20px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Hola! 👋
                            </p>
                            <p style="margin:0 0 24px 0;font-size:15px;line-height:22px;color:#334155;">
                                Te confirmamos que tu reserva ha sido creada exitosamente. A continuación encontrarás los detalles:
                            </p>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;margin:0 0 24px 0;">
                                <tr>
                                    <td style="padding:20px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;width:120px;">
                                                    <strong style="color:#475569;">📍 Espacio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Inicio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Fin:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <div style="background:#f0f9ff;border-left:4px solid #3b82f6;padding:16px;border-radius:4px;margin:0 0 24px 0;">
                                <p style="margin:0;font-size:13px;line-height:20px;color:#1e40af;">
                                    <strong>💡 Importante:</strong> Recibirás un código QR para validar tu asistencia el día de la reserva.
                                </p>
                            </div>
                            
                            <p style="margin:0 0 8px 0;font-size:14px;line-height:20px;color:#64748b;">
                                Gracias por usar nuestro sistema de reservas.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:0 28px 28px 28px;">
                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;">
                            <p style="margin:0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © 2025 Municipalidad de Pérez Zeledón · Sistema de Reservas
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
                """.formatted(space, starts, ends);
    }
    
    private String buildHtmlStatusChanged(String oldStatus, String newStatus, String space, String starts, String ends) {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <title>Estado de Reserva Actualizado</title>
</head>
<body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#f5f7fa;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background:#f5f7fa;padding:24px 0;">
        <tr>
            <td align="center">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #e6e9ef;box-shadow:0 2px 8px rgba(0,0,0,0.04);">
                    <tr>
                        <td style="padding:32px 28px 16px 28px;background:linear-gradient(135deg,#8b5cf6 0%%,#7c3aed 100%%);border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;line-height:32px;color:#ffffff;font-weight:600;">
                                🔄 Estado Actualizado
                            </h1>
                            <p style="margin:8px 0 0 0;font-size:14px;line-height:20px;color:#ede9fe;">
                                El estado de tu reserva ha cambiado
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px;">
                            <p style="margin:0 0 20px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Hola! 👋
                            </p>
                            <p style="margin:0 0 24px 0;font-size:15px;line-height:22px;color:#334155;">
                                Te informamos que el estado de tu reserva ha sido actualizado:
                            </p>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="margin:0 0 24px 0;">
                                <tr>
                                    <td align="center" style="padding:20px;background:#fef3c7;border-radius:8px;border:1px solid #fcd34d;">
                                        <p style="margin:0 0 8px 0;font-size:13px;color:#92400e;font-weight:600;">ESTADO ANTERIOR</p>
                                        <p style="margin:0;font-size:18px;color:#78350f;font-weight:700;">%s</p>
                                    </td>
                                    <td align="center" style="padding:0 16px;">
                                        <span style="font-size:24px;color:#9ca3af;">→</span>
                                    </td>
                                    <td align="center" style="padding:20px;background:#dcfce7;border-radius:8px;border:1px solid #86efac;">
                                        <p style="margin:0 0 8px 0;font-size:13px;color:#166534;font-weight:600;">ESTADO ACTUAL</p>
                                        <p style="margin:0;font-size:18px;color:#14532d;font-weight:700;">%s</p>
                                    </td>
                                </tr>
                            </table>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;margin:0 0 24px 0;">
                                <tr>
                                    <td style="padding:20px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;width:120px;">
                                                    <strong style="color:#475569;">📍 Espacio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Inicio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Fin:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:0 28px 28px 28px;">
                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;">
                            <p style="margin:0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © 2025 Municipalidad de Pérez Zeledón · Sistema de Reservas
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
                """.formatted(oldStatus, newStatus, space, starts, ends);
    }
    
    private String buildHtmlCancelled(String reason, String space, String starts, String ends) {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <title>Reserva Cancelada</title>
</head>
<body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#f5f7fa;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background:#f5f7fa;padding:24px 0;">
        <tr>
            <td align="center">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #e6e9ef;box-shadow:0 2px 8px rgba(0,0,0,0.04);">
                    <tr>
                        <td style="padding:32px 28px 16px 28px;background:linear-gradient(135deg,#dc2626 0%%,#b91c1c 100%%);border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;line-height:32px;color:#ffffff;font-weight:600;">
                                ❌ Reserva Cancelada
                            </h1>
                            <p style="margin:8px 0 0 0;font-size:14px;line-height:20px;color:#fecaca;">
                                Tu reserva ha sido cancelada
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px;">
                            <p style="margin:0 0 20px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Hola! 👋
                            </p>
                            <p style="margin:0 0 24px 0;font-size:15px;line-height:22px;color:#334155;">
                                Lamentamos informarte que tu reserva ha sido cancelada.
                            </p>
                            
                            <div style="background:#fee2e2;border-left:4px solid #ef4444;padding:16px;border-radius:4px;margin:0 0 24px 0;">
                                <p style="margin:0 0 4px 0;font-size:13px;line-height:20px;color:#7f1d1d;font-weight:600;">
                                    Motivo de cancelación:
                                </p>
                                <p style="margin:0;font-size:14px;line-height:20px;color:#991b1b;">
                                    %s
                                </p>
                            </div>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;margin:0 0 24px 0;">
                                <tr>
                                    <td style="padding:20px;">
                                        <p style="margin:0 0 12px 0;font-size:13px;color:#64748b;font-weight:600;">DETALLES DE LA RESERVA CANCELADA</p>
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;width:120px;">
                                                    <strong style="color:#475569;">📍 Espacio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Inicio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Fin:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="margin:0;font-size:14px;line-height:20px;color:#64748b;">
                                Si tienes alguna duda o necesitas realizar una nueva reserva, no dudes en contactarnos.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:0 28px 28px 28px;">
                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;">
                            <p style="margin:0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © 2025 Municipalidad de Pérez Zeledón · Sistema de Reservas
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
                """.formatted(reason, space, starts, ends);
    }
    
    private String buildHtmlQrValidated(String space, String starts) {
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <title>Asistencia Confirmada</title>
</head>
<body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#f5f7fa;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background:#f5f7fa;padding:24px 0;">
        <tr>
            <td align="center">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #e6e9ef;box-shadow:0 2px 8px rgba(0,0,0,0.04);">
                    <tr>
                        <td style="padding:32px 28px 16px 28px;background:linear-gradient(135deg,#10b981 0%%,#059669 100%%);border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;line-height:32px;color:#ffffff;font-weight:600;">
                                ✨ Asistencia Confirmada
                            </h1>
                            <p style="margin:8px 0 0 0;font-size:14px;line-height:20px;color:#d1fae5;">
                                Tu asistencia ha sido registrada exitosamente
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px;">
                            <p style="margin:0 0 20px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Hola! 👋
                            </p>
                            <p style="margin:0 0 24px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Excelente! Hemos registrado tu asistencia mediante el código QR.
                            </p>
                            
                            <div style="background:#d1fae5;border-left:4px solid #10b981;padding:16px;border-radius:4px;margin:0 0 24px 0;">
                                <p style="margin:0;font-size:14px;line-height:20px;color:#065f46;">
                                    <strong>✓ Confirmado:</strong> Tu presencia ha quedado registrada en el sistema.
                                </p>
                            </div>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f8fafc;border-radius:8px;border:1px solid #e2e8f0;margin:0 0 24px 0;">
                                <tr>
                                    <td style="padding:20px;">
                                        <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;width:120px;">
                                                    <strong style="color:#475569;">📍 Espacio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;color:#64748b;">
                                                    <strong style="color:#475569;">🕐 Inicio:</strong>
                                                </td>
                                                <td style="padding:8px 0;font-size:14px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="margin:0;font-size:14px;line-height:20px;color:#64748b;">
                                Gracias por utilizar nuestras instalaciones. ¡Que tengas una excelente experiencia!
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:0 28px 28px 28px;">
                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;">
                            <p style="margin:0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © 2025 Municipalidad de Pérez Zeledón · Sistema de Reservas
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
                """.formatted(space, starts);
    }
    
    private String buildHtmlRoleChanged(String userName, String oldRole, String newRole, String newRoleName) {
        // Determinar el emoji y color según el rol
        String roleEmoji = switch (newRole) {
            case "ROLE_ADMIN" -> "👑";
            case "ROLE_SUPERVISOR" -> "⭐";
            default -> "👤";
        };
        
        String roleColor = switch (newRole) {
            case "ROLE_ADMIN" -> "#dc2626"; // Rojo
            case "ROLE_SUPERVISOR" -> "#ea580c"; // Naranja
            default -> "#2563eb"; // Azul
        };
        
        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light dark">
    <title>Rol Actualizado</title>
</head>
<body style="margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;background:#f5f7fa;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background:#f5f7fa;padding:24px 0;">
        <tr>
            <td align="center">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="width:100%%;max-width:600px;background:#ffffff;border-radius:12px;border:1px solid #e6e9ef;box-shadow:0 2px 8px rgba(0,0,0,0.04);">
                    <tr>
                        <td style="padding:32px 28px 16px 28px;background:linear-gradient(135deg,%s 0%%,%s 100%%);border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;line-height:32px;color:#ffffff;font-weight:600;">
                                %s Tu Rol ha sido Actualizado
                            </h1>
                            <p style="margin:8px 0 0 0;font-size:14px;line-height:20px;color:rgba(255,255,255,0.9);">
                                Tienes nuevos permisos en el sistema
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:28px;">
                            <p style="margin:0 0 20px 0;font-size:15px;line-height:22px;color:#334155;">
                                ¡Hola <strong>%s</strong>! 👋
                            </p>
                            <p style="margin:0 0 24px 0;font-size:15px;line-height:22px;color:#334155;">
                                Te informamos que tu rol en el sistema ha sido actualizado por un administrador.
                            </p>
                            
                            <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="margin:0 0 24px 0;">
                                <tr>
                                    <td align="center" style="padding:20px;background:#fef3c7;border-radius:8px;border:1px solid #fcd34d;">
                                        <p style="margin:0 0 8px 0;font-size:13px;color:#92400e;font-weight:600;">ROL ANTERIOR</p>
                                        <p style="margin:0;font-size:16px;color:#78350f;font-weight:600;">%s</p>
                                    </td>
                                    <td align="center" style="padding:0 16px;">
                                        <span style="font-size:24px;color:#9ca3af;">→</span>
                                    </td>
                                    <td align="center" style="padding:20px;background:#dbeafe;border-radius:8px;border:2px solid %s;">
                                        <p style="margin:0 0 8px 0;font-size:13px;color:#1e3a8a;font-weight:600;">NUEVO ROL</p>
                                        <p style="margin:0;font-size:20px;font-weight:700;" style="color:%s;">
                                            %s %s
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <div style="background:#e0f2fe;border-left:4px solid #0284c7;padding:16px;border-radius:4px;margin:0 0 24px 0;">
                                <p style="margin:0 0 8px 0;font-size:14px;line-height:20px;color:#075985;font-weight:600;">
                                    🎯 Permisos de tu nuevo rol:
                                </p>
                                %s
                            </div>
                            
                            <p style="margin:0 0 8px 0;font-size:14px;line-height:20px;color:#64748b;">
                                Los cambios son efectivos de inmediato. Por favor, cierra sesión y vuelve a iniciar para que los nuevos permisos se apliquen correctamente.
                            </p>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding:0 28px 28px 28px;">
                            <hr style="border:none;border-top:1px solid #e2e8f0;margin:0 0 20px 0;">
                            <p style="margin:0;font-size:12px;line-height:18px;color:#94a3b8;text-align:center;">
                                © 2025 Municipalidad de Pérez Zeledón · Sistema de Reservas
                            </p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
                """.formatted(
                    roleColor, darkenColor(roleColor), // Gradiente de header
                    roleEmoji,
                    userName,
                    oldRole.replace("ROLE_", ""),
                    roleColor,
                    roleColor,
                    roleEmoji, newRoleName,
                    getPermissionsHtml(newRole)
                );
    }
    
    private String darkenColor(String hexColor) {
        // Oscurece un color hex para el gradiente
        return switch (hexColor) {
            case "#dc2626" -> "#b91c1c";
            case "#ea580c" -> "#c2410c";
            default -> "#1d4ed8";
        };
    }
    
    private String getPermissionsHtml(String role) {
        return switch (role) {
            case "ROLE_ADMIN" -> """
                <ul style="margin:8px 0 0 0;padding-left:24px;font-size:13px;line-height:24px;color:#0c4a6e;">
                    <li>Gestión completa de usuarios y roles</li>
                    <li>Administración de espacios y horarios</li>
                    <li>Gestión total de reservas</li>
                    <li>Acceso a dashboard y métricas</li>
                    <li>Cancelación sin restricciones de tiempo</li>
                    <li>Exportación de datos de cualquier usuario</li>
                </ul>
                """;
            case "ROLE_SUPERVISOR" -> """
                <ul style="margin:8px 0 0 0;padding-left:24px;font-size:13px;line-height:24px;color:#0c4a6e;">
                    <li>Visualización y gestión de reservas</li>
                    <li>Gestión de horarios de espacios</li>
                    <li>Acceso a dashboard y métricas</li>
                    <li>Exportación de datos de usuarios</li>
                    <li>Validación de códigos QR</li>
                </ul>
                """;
            default -> """
                <ul style="margin:8px 0 0 0;padding-left:24px;font-size:13px;line-height:24px;color:#0c4a6e;">
                    <li>Crear y gestionar tus propias reservas</li>
                    <li>Consultar espacios disponibles</li>
                    <li>Crear reseñas de espacios utilizados</li>
                    <li>Exportar tus propias reservas</li>
                    <li>Ver y usar códigos QR de tus reservas</li>
                </ul>
                """;
        };
    }
}
