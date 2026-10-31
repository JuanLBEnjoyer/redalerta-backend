package redalerta_backend.common.mail.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import redalerta_backend.common.mail.EmailService;

/**
 * Implementacion del servicio de correo electronico con tolerancia a fallos en
 * desarrollo.
 * Si las credenciales SMTP no estan parametrizadas o la conexion falla,
 * registra el codigo
 * OTP en los logs de consola para permitir la realizacion de pruebas locales
 * sin bloqueos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String remitente;

    @Override
    public void enviarCodigoRecuperacion(String destinatario, String nombreUsuario, String codigoOtp) {
        if (remitente == null || remitente.isBlank() || remitente.contains("@smtp-brevo.com")) {
            log.warn("[CONFIGURACION INCOMPLETA] El remitente '{}' no es un correo valido. " +
                    "Configure la propiedad 'app.mail.from' con el correo electronico verificado en Brevo. " +
                    "[MODO DESARROLLO] Codigo OTP para {}: {}", remitente, destinatario, codigoOtp);
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente, "RedAlerta Soporte");
            helper.setTo(destinatario);
            helper.setSubject("RedAlerta - Codigo de Recuperacion de Contrasena");

            String contenidoHtml = construirCuerpoHtml(nombreUsuario, codigoOtp);
            helper.setText(contenidoHtml, true);

            mailSender.send(mensaje);
            log.info("Correo de recuperacion enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.warn("Fallo el envio SMTP a {} ({}). [MODO DESARROLLO] Codigo OTP: {}",
                    destinatario, e.getMessage(), codigoOtp);
        }
    }

    private String construirCuerpoHtml(String nombreUsuario, String codigoOtp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #c62828; text-align: center;">RedAlerta Comunitaria</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Has solicitado restablecer tu contrasena de acceso a la plataforma RedAlerta.</p>
                    <p>Tu codigo de recuperacion de 6 digitos es:</p>
                    <div style="background-color: #f5f5f5; border: 2px dashed #c62828; border-radius: 6px; padding: 16px; text-align: center; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #c62828;">%s</span>
                    </div>
                    <p style="color: #666; font-size: 14px;">Este codigo es valido durante <strong>10 minutos</strong> y puede usarse una sola vez.</p>
                    <p style="color: #888; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 10px;">
                        Si tu no solicitaste este cambio, puedes ignorar este mensaje de forma segura. Tu cuenta permanece protegida.
                    </p>
                </div>
                """
                .formatted(
                        nombreUsuario != null ? nombreUsuario : "Vecino/a",
                        codigoOtp);
    }
}
