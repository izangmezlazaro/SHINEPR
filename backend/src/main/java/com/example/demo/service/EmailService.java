package com.example.demo.service;

import com.example.demo.util.AppConfig;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de envío de correos electrónicos mediante Jakarta Mail (JavaMail).
 *
 * Configuración requerida en app.properties (o variables de entorno equivalentes):
 *   mail.host       → SMTP host  (ej. smtp.gmail.com)
 *   mail.port       → SMTP port  (ej. 587)
 *   mail.username   → usuario autenticado en el SMTP
 *   mail.password   → contraseña / app-password
 *   mail.from       → dirección remitente  (ej. noreply@shine-parfums.com)
 *   mail.starttls   → true/false (activar STARTTLS, recomendado)
 */
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class.getName());

    private final Session session;
    private final String  fromAddress;

    public EmailService() {
        fromAddress = AppConfig.get("mail.from");

        Properties props = new Properties();
        props.put("mail.smtp.host",            AppConfig.get("mail.host"));
        props.put("mail.smtp.port",            AppConfig.get("mail.port"));
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", AppConfig.get("mail.starttls"));

        String username = AppConfig.get("mail.username");
        String password = AppConfig.get("mail.password");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Envía el correo de confirmación de pago / factura al cliente.
     *
     * @param destinatario  email del cliente
     * @param nombreCliente nombre del cliente para el saludo
     * @param idPedido      identificador del pedido (ej. 1234)
     * @param importe       importe pagado
     */
    public void enviarFacturaBizum(String destinatario,
                                   String nombreCliente,
                                   Integer idPedido,
                                   BigDecimal importe) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, "SHINE Parfums"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("✅ Pago confirmado — Pedido SHINE-" + idPedido);

            String htmlBody = buildHtmlBody(nombreCliente, idPedido, importe);

            // Cuerpo en HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=UTF-8");

            Multipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);

            LOG.info("[EmailService] Factura enviada a " + destinatario + " para pedido SHINE-" + idPedido);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // No propagamos: el webhook ya respondió 200 a bunq, el email es best-effort
            LOG.log(Level.SEVERE, "[EmailService] Error al enviar factura para pedido SHINE-" + idPedido, e);
        }
    }

    // ── Construcción del cuerpo HTML ──────────────────────────────────────────

    private String buildHtmlBody(String nombreCliente, Integer idPedido, BigDecimal importe) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#0d0d0d;font-family:'Helvetica Neue',Arial,sans-serif;color:#f0f0f0;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px;margin:40px auto;background:#1a1a1a;border-radius:12px;overflow:hidden;">
                    <tr>
                      <td style="background:linear-gradient(135deg,#b8963e,#f5e29e,#b8963e);padding:40px;text-align:center;">
                        <h1 style="margin:0;font-size:32px;letter-spacing:6px;color:#0d0d0d;">SHINE</h1>
                        <p style="margin:8px 0 0;font-size:12px;letter-spacing:3px;color:#0d0d0d;text-transform:uppercase;">Parfums de Luxe</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:40px 48px;">
                        <h2 style="color:#c9a84c;font-size:20px;margin:0 0 16px;">Pago recibido con éxito</h2>
                        <p style="margin:0 0 24px;line-height:1.6;color:#ccc;">
                          Hola <strong style="color:#f0f0f0;">%s</strong>,<br>
                          hemos recibido tu pago mediante <strong>Bizum</strong>. Tu pedido ya está siendo procesado
                          por nuestro equipo.
                        </p>
                        <table width="100%%" style="border-collapse:collapse;background:#111;border-radius:8px;overflow:hidden;">
                          <tr>
                            <td style="padding:14px 20px;border-bottom:1px solid #2a2a2a;color:#888;font-size:13px;">Referencia de pedido</td>
                            <td style="padding:14px 20px;border-bottom:1px solid #2a2a2a;text-align:right;font-weight:bold;color:#f0f0f0;">SHINE-%d</td>
                          </tr>
                          <tr>
                            <td style="padding:14px 20px;color:#888;font-size:13px;">Importe pagado</td>
                            <td style="padding:14px 20px;text-align:right;font-weight:bold;font-size:18px;color:#c9a84c;">%.2f €</td>
                          </tr>
                        </table>
                        <p style="margin:32px 0 0;font-size:13px;color:#666;line-height:1.6;">
                          Si tienes cualquier duda, responde a este correo o contáctanos en
                          <a href="mailto:info@shine-parfums.com" style="color:#c9a84c;">info@shine-parfums.com</a>.
                        </p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:20px 48px;border-top:1px solid #2a2a2a;text-align:center;font-size:11px;color:#444;">
                        © 2025 SHINE Parfums · Todos los derechos reservados
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(nombreCliente, idPedido, importe);
    }
}
