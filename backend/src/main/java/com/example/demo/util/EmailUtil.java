package com.example.demo.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Utilidad para enviar emails de confirmación de pago usando Jakarta Mail.
 *
 * Configuración via app.properties o variables de entorno:
 *   mail.smtp.host       → Servidor SMTP (p.ej. smtp.gmail.com)
 *   mail.smtp.port       → Puerto SMTP (p.ej. 587)
 *   mail.smtp.user       → Usuario SMTP (tu email)
 *   mail.smtp.password   → Contraseña SMTP o App Password
 *   mail.from.address    → Dirección de envío (p.ej. noreply@shine.com)
 *   mail.from.name       → Nombre del remitente (p.ej. Shine Boutique)
 */
public final class EmailUtil {

    private EmailUtil() {}

    /**
     * Envía un email HTML de confirmación de pago Bizum al cliente.
     *
     * @param toEmail      Email del cliente
     * @param toName       Nombre del cliente
     * @param idPedido     ID numérico del pedido
     * @param totalStr     Total del pedido formateado (p.ej. "49.95")
     */
    public static void enviarConfirmacionBizum(String toEmail, String toName,
                                                int idPedido, String totalStr, byte[] pdfBytes) {
        try {
            String smtpHost     = AppConfig.get("mail.smtp.host");
            String smtpPort     = AppConfig.get("mail.smtp.port");
            String smtpUser     = AppConfig.get("mail.smtp.user");
            String smtpPassword = AppConfig.get("mail.smtp.password");
            String fromAddress  = AppConfig.get("mail.from.address");
            String fromName     = AppConfig.get("mail.from.name");

            Properties props = new Properties();
            props.put("mail.smtp.auth",                 "true");
            props.put("mail.smtp.starttls.enable",      "true");
            props.put("mail.smtp.host",                 smtpHost);
            props.put("mail.smtp.port",                 smtpPort);
            props.put("mail.smtp.ssl.trust",            smtpHost);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toEmail));
            message.setSubject("✅ Pago Bizum confirmado — Pedido #SHINE-" + idPedido);

            String html = buildHtmlBody(toName, idPedido, totalStr);
            
            // Usar Multipart para soportar texto HTML y adjuntos
            Multipart multipart = new MimeMultipart();
            
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(html, "text/html; charset=UTF-8");
            multipart.addBodyPart(textPart);
            
            // Adjuntar la factura PDF si se proporciona
            if (pdfBytes != null && pdfBytes.length > 0) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                jakarta.mail.util.ByteArrayDataSource bds = new jakarta.mail.util.ByteArrayDataSource(pdfBytes, "application/pdf");
                attachmentPart.setDataHandler(new jakarta.activation.DataHandler(bds));
                attachmentPart.setFileName("Factura_SHINE-" + idPedido + ".pdf");
                attachmentPart.setDisposition(Part.ATTACHMENT); // <-- Evita que marque como spam por adjunto malformado
                multipart.addBodyPart(attachmentPart);
            }
            
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("[EmailUtil] Email de confirmación enviado a: " + toEmail);

        } catch (Exception e) {
            // No propagamos el error de email para no bloquear la confirmación del pago
            System.err.println("[EmailUtil] Error enviando email a " + toEmail + ": " + e.getMessage());
        }
    }

    private static String buildHtmlBody(String nombre, int idPedido, String total) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
                <title>Confirmación de Pago — Shine</title></head>
                <body style="margin:0;padding:0;background:#0a0a0a;font-family:'Helvetica Neue',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0a0a0a;padding:40px 20px;">
                    <tr><td align="center">
                      <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#111;border-radius:16px;overflow:hidden;border:1px solid #222;">
                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#1a1a1a 0%%,#2a2a2a 100%%);padding:40px 48px;text-align:center;border-bottom:1px solid #222;">
                            <div style="font-size:28px;font-weight:700;color:#fff;letter-spacing:6px;text-transform:uppercase;">SHINE</div>
                            <div style="font-size:11px;color:#888;letter-spacing:3px;margin-top:4px;">LUXURY BEAUTY</div>
                          </td>
                        </tr>
                        <!-- Check Icon -->
                        <tr>
                          <td style="padding:40px 48px 20px;text-align:center;">
                            <div style="width:72px;height:72px;background:linear-gradient(135deg,#16a34a,#22c55e);border-radius:50%%;margin:0 auto 24px;display:flex;align-items:center;justify-content:center;font-size:36px;">✓</div>
                            <h1 style="margin:0 0 12px;font-size:26px;font-weight:700;color:#fff;">¡Pago Confirmado!</h1>
                            <p style="margin:0;color:#888;font-size:15px;line-height:1.6;">Tu pago por Bizum ha sido validado por el equipo de Shine.</p>
                          </td>
                        </tr>
                        <!-- Order Details -->
                        <tr>
                          <td style="padding:0 48px 32px;">
                            <table width="100%%" style="background:#1a1a1a;border-radius:12px;overflow:hidden;border:1px solid #2a2a2a;">
                              <tr>
                                <td style="padding:20px 24px;border-bottom:1px solid #2a2a2a;">
                                  <div style="font-size:11px;color:#888;letter-spacing:1px;text-transform:uppercase;margin-bottom:6px;">Cliente</div>
                                  <div style="font-size:16px;font-weight:600;color:#fff;">%s</div>
                                </td>
                              </tr>
                              <tr>
                                <td style="padding:20px 24px;border-bottom:1px solid #2a2a2a;">
                                  <div style="font-size:11px;color:#888;letter-spacing:1px;text-transform:uppercase;margin-bottom:6px;">Número de Pedido</div>
                                  <div style="font-size:20px;font-weight:700;color:#c9a96e;letter-spacing:1px;">#SHINE-%d</div>
                                </td>
                              </tr>
                              <tr>
                                <td style="padding:20px 24px;">
                                  <div style="font-size:11px;color:#888;letter-spacing:1px;text-transform:uppercase;margin-bottom:6px;">Total Pagado</div>
                                  <div style="font-size:24px;font-weight:700;color:#22c55e;">€%s</div>
                                </td>
                              </tr>
                            </table>
                          </td>
                        </tr>
                        <!-- Message -->
                        <tr>
                          <td style="padding:0 48px 32px;">
                            <div style="background:#0f2d1a;border:1px solid #16a34a30;border-radius:12px;padding:20px 24px;">
                              <p style="margin:0;color:#86efac;font-size:14px;line-height:1.7;">
                                Tu pedido está siendo preparado con todo el cuidado que mereces. 
                                Recibirás una notificación cuando sea enviado. 
                                Si tienes alguna duda, no dudes en contactarnos.
                              </p>
                            </div>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="padding:24px 48px;text-align:center;border-top:1px solid #222;">
                            <p style="margin:0 0 8px;font-size:12px;color:#555;">Este email fue generado automáticamente — por favor no respondas.</p>
                            <p style="margin:0;font-size:12px;color:#555;">© 2026 Shine Luxury Beauty. Todos los derechos reservados.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(nombre, idPedido, total);
    }
}
