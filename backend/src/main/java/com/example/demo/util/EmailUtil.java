package com.example.demo.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Utilidad para enviar emails de confirmación de pago usando Jakarta Mail.
 *
 * Configuración via app.properties o variables de entorno:
 * mail.smtp.host → Servidor SMTP (p.ej. smtp.gmail.com)
 * mail.smtp.port → Puerto SMTP (p.ej. 587)
 * mail.smtp.user → Usuario SMTP (tu email)
 * mail.smtp.password → Contraseña SMTP o App Password
 * mail.from.address → Dirección de envío (p.ej. noreply@shine.com)
 * mail.from.name → Nombre del remitente (p.ej. Shine Boutique)
 */
public final class EmailUtil {

  private EmailUtil() {
  }

  /**
   * Envía un email HTML de confirmación de pago Bizum al cliente.
   *
   * @param toEmail  Email del cliente
   * @param toName   Nombre del cliente
   * @param idPedido ID numérico del pedido
   * @param totalStr Total del pedido formateado (p.ej. "49.95")
   */
  public static void enviarConfirmacionBizum(String toEmail, String toName,
      int idPedido, String totalStr, byte[] pdfBytes) {
    try {
      String smtpHost = AppConfig.get("mail.smtp.host");
      String smtpPort = AppConfig.get("mail.smtp.port");
      String smtpUser = AppConfig.get("mail.smtp.user");
      String smtpPassword = AppConfig.get("mail.smtp.password");
      String fromAddress = AppConfig.get("mail.from.address");
      String fromName = AppConfig.get("mail.from.name");

      Properties props = new Properties();
      props.put("mail.smtp.auth", "true");
      props.put("mail.smtp.starttls.enable", "true");
      props.put("mail.smtp.host", smtpHost);
      props.put("mail.smtp.port", smtpPort);
      props.put("mail.smtp.ssl.trust", smtpHost);

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
        jakarta.mail.util.ByteArrayDataSource bds = new jakarta.mail.util.ByteArrayDataSource(pdfBytes,
            "application/pdf");
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
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width,initial-scale=1.0">
          <title>Confirmación de Pago — Shine Luxury Beauty</title>
        </head>
        <body style="margin:0;padding:0;background-color:#050505;font-family:'Helvetica Neue', Helvetica, Arial, sans-serif;-webkit-font-smoothing:antialiased;">
          <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#050505;padding:40px 20px;">
            <tr><td align="center">

              <table width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background-color:#0a0a0a;border:1px solid #222222;border-radius:2px;">
                <tr>
                  <td style="padding:50px 40px 30px;text-align:center;">
                    <div style="font-family:'Didot','Playfair Display','Times New Roman',serif;font-size:32px;color:#ffffff;letter-spacing:10px;text-transform:uppercase;margin-left:10px;">SHINE</div>
                    <div style="font-size:10px;color:#c9a96e;letter-spacing:4px;text-transform:uppercase;margin-top:10px;">Parfums Sur Mesure</div>
                  </td>
                </tr>

                <tr>
                  <td style="padding:10px 40px 20px;text-align:center;">
                    <div style="width:60px;height:60px;border:1px solid #c9a96e;border-radius:50%%;margin:0 auto;display:table;">
                      <div style="display:table-cell;vertical-align:middle;font-size:24px;color:#c9a96e;">✓</div>
                    </div>
                    <h1 style="font-family:'Didot','Playfair Display','Times New Roman',serif;font-size:24px;color:#ffffff;font-weight:normal;margin:25px 0 10px;letter-spacing:1px;">Pago Confirmado</h1>
                    <p style="margin:0;color:#888888;font-size:14px;line-height:1.6;font-weight:300;">
                      Estimado/a <span style="color:#ffffff;">%s</span>,<br>
                      Hemos recibido tu pago por Bizum. Tu pedido ya está en nuestro atelier.
                    </p>
                  </td>
                </tr>

                <tr>
                  <td style="padding:20px 40px 40px;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="border-top:1px solid #222222;border-bottom:1px solid #222222;padding:25px 0;">
                      <tr>
                        <td style="padding-bottom:15px;font-size:12px;color:#666666;text-transform:uppercase;letter-spacing:1px;">Nº de Pedido</td>
                        <td align="right" style="padding-bottom:15px;font-size:16px;color:#ffffff;font-family:monospace;letter-spacing:1px;">#SHINE-%d</td>
                      </tr>
                      <tr>
                        <td style="padding-bottom:15px;font-size:12px;color:#666666;text-transform:uppercase;letter-spacing:1px;">Método de Pago</td>
                        <td align="right" style="padding-bottom:15px;font-size:14px;color:#ffffff;">Bizum</td>
                      </tr>
                      <tr>
                        <td style="padding-top:15px;border-top:1px dashed #333333;font-size:12px;color:#c9a96e;text-transform:uppercase;letter-spacing:1px;font-weight:bold;">Total Pagado</td>
                        <td align="right" style="padding-top:15px;border-top:1px dashed #333333;font-size:22px;color:#c9a96e;font-family:'Didot','Playfair Display',serif;">%s €</td>
                      </tr>
                    </table>
                  </td>
                </tr>

                <tr>
                  <td style="padding:0 40px 40px;text-align:center;">
                    <a href="https://shine-parfums.space/pedidos" style="display:inline-block;background-color:#ffffff;color:#000000;font-size:12px;font-weight:bold;letter-spacing:2px;text-transform:uppercase;text-decoration:none;padding:16px 36px;border-radius:2px;transition:all 0.3s ease;">
                      Ver estado del pedido
                    </a>
                  </td>
                </tr>

                <tr>
                  <td style="padding:0 40px 40px;">
                    <div style="background-color:#0c0c0c;border-left:2px solid #c9a96e;padding:20px;">
                      <p style="margin:0;color:#999999;font-size:13px;line-height:1.7;font-weight:300;">
                        <strong style="color:#ffffff;font-weight:normal;">El arte de la espera.</strong><br>
                        Nuestros perfumistas comenzarán en breve a formular tu creación personalizada. Recibirás un nuevo aviso en cuanto tu fragancia inicie su viaje hacia ti.
                      </p>
                    </div>
                  </td>
                </tr>

                <tr>
                  <td style="padding:30px 40px;background-color:#080808;text-align:center;border-top:1px solid #1a1a1a;">
                    <div style="margin-bottom:15px;">
                      <a href="#" style="color:#666666;text-decoration:none;font-size:12px;letter-spacing:1px;margin:0 10px;">INSTAGRAM</a>
                      <a href="#" style="color:#666666;text-decoration:none;font-size:12px;letter-spacing:1px;margin:0 10px;">TIKTOK</a>
                    </div>
                    <p style="margin:0 0 10px;font-size:11px;color:#444444;line-height:1.5;">
                      Si tienes alguna consulta, responde directamente a este correo o visita nuestro <a href="#" style="color:#c9a96e;text-decoration:none;">Centro de Atención</a>.
                    </p>
                    <p style="margin:0;font-size:10px;color:#333333;text-transform:uppercase;letter-spacing:2px;">
                      © 2026 Shine LLC. All rights reserved.
                    </p>
                  </td>
                </tr>
              </table>

            </td></tr>
          </table>
        </body>
        </html>
        """
        .formatted(nombre, idPedido, total);
  }
}
