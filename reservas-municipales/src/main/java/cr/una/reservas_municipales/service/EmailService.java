package cr.una.reservas_municipales.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Locale;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        log.info("EmailService initialized with TemplateEngine: {}", templateEngine.getClass().getName());
    }

    /**
     * Envía un email usando una plantilla Thymeleaf
     */
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) throws MessagingException {
        log.debug("Preparing to send email to: {} with template: {}", to, templateName);
        log.debug("Template variables: {}", variables);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("reservas.muni.pz@gmail.com");

        // Procesar la plantilla con locale español
        Context context = new Context(Locale.of("es", "CR"));
        context.setVariables(variables);
        
        log.debug("Processing Thymeleaf template: {}", templateName);
        String htmlContent = templateEngine.process(templateName, context);
        
        log.debug("Template processed successfully. HTML length: {} characters", htmlContent.length());
        log.trace("HTML Content preview (first 500 chars): {}", 
                  htmlContent.length() > 500 ? htmlContent.substring(0, 500) : htmlContent);

        helper.setText(htmlContent, true); // true = HTML

        log.debug("Sending email...");
        mailSender.send(message);
        log.info("Email sent successfully to: {}", to);
    }

    /**
     * Envía un email con un archivo embebido (como imagen QR)
     */
    public void sendEmailWithEmbeddedImage(String to, String subject, String templateName, 
                                           Map<String, Object> variables, byte[] imageBytes, 
                                           String imageName) throws MessagingException {
        log.debug("Preparing to send email with embedded image to: {} with template: {}", to, templateName);
        log.debug("Template variables: {}", variables);
        log.debug("Image size: {} bytes, CID: {}", imageBytes != null ? imageBytes.length : 0, imageName);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom("reservas.muni.pz@gmail.com");

        // Procesar la plantilla con locale español
        Context context = new Context(Locale.of("es", "CR"));
        context.setVariables(variables);
        
        log.debug("Processing Thymeleaf template: {}", templateName);
        String htmlContent = templateEngine.process(templateName, context);
        
        log.debug("Template processed successfully. HTML length: {} characters", htmlContent.length());
        log.trace("HTML Content preview (first 500 chars): {}", 
                  htmlContent.length() > 500 ? htmlContent.substring(0, 500) : htmlContent);

        helper.setText(htmlContent, true); // true = HTML

        // Agregar la imagen embebida
        if (imageBytes != null && imageBytes.length > 0) {
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);
            helper.addInline(imageName, imageResource, "image/png");
            log.debug("Embedded image added with CID: {}", imageName);
        } else {
            log.warn("Image bytes are null or empty, skipping embedded image");
        }

        log.debug("Sending email with embedded image...");
        mailSender.send(message);
        log.info("Email with embedded image sent successfully to: {}", to);
    }
}
