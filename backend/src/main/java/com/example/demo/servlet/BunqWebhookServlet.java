package com.example.demo.servlet;

import com.example.demo.dao.PedidoDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.Usuario;
import com.example.demo.service.EmailService;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *  BunqWebhookServlet  —  POST /api/webhooks/bunq
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  Recibe los Callbacks (Webhooks) que bunq envía cuando se produce un evento
 *  de tipo "PAYMENT" o "MUTATION" en la cuenta.
 *
 *  Flujo completo:
 *  1. Leer y parsear el JSON enviado por bunq.
 *  2. Extraer el importe (amount.value) y la descripción del pago.
 *  3. Buscar en la descripción el patrón  SHINE-[A-Z0-9]+  con Regex.
 *  4. Si se encuentra el código de pedido:
 *       a. Consultar el pedido en PostgreSQL via PedidoDAO.
 *       b. Verificar que el estado sea "PENDIENTE_BIZUM" y el importe coincida.
 *       c. Actualizar el estado a "PAGADO".
 *       d. Enviar correo de factura al cliente via EmailService.
 *  5. Siempre devolver HTTP 200 OK para que bunq considere el callback como
 *     recibido (si devolvemos un error, bunq reintentará indefinidamente).
 *
 *  ┌──────────────────────────────────────────────────────────────────────┐
 *  │  REGISTRO DEL WEBHOOK EN BUNQ                                        │
 *  │                                                                      │
 *  │  1. Accede a la bunq Developer Portal:                               │
 *  │       https://developer.bunq.com                                     │
 *  │  2. Ve a tu aplicación → "Callbacks".                                │
 *  │  3. Añade una nueva URL de notificación:                             │
 *  │       URL:      https://tu-servidor.com/api/webhooks/bunq            │
 *  │       Categoría: PAYMENT  (y/o MUTATION)                             │
 *  │  4. Método vía API (recomendado en producción):                      │
 *  │       PUT /v1/user/{userId}/notification-filter-url                  │
 *  │       Body: { "notification_filters": [                              │
 *  │               { "notification_delivery_method": "URL",               │
 *  │                 "notification_target": "https://tu-server.com/api/webhooks/bunq",
 *  │                 "category": "PAYMENT" }                              │
 *  │             ]}                                                       │
 *  │  5. Asegúrate de que tu servidor tenga HTTPS válido (bunq lo exige). │
 *  └──────────────────────────────────────────────────────────────────────┘
 */
@WebServlet(urlPatterns = "/api/webhooks/bunq", name = "BunqWebhookServlet")
public class BunqWebhookServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BunqWebhookServlet.class.getName());

    /** Patrón regex para aislar el código de pedido dentro de la descripción. */
    private static final Pattern PEDIDO_PATTERN = Pattern.compile("SHINE-([A-Z0-9]+)");

    /** Estado que debe tener el pedido para ser procesado por este webhook. */
    private static final String ESTADO_PENDIENTE = "PENDIENTE_BIZUM";

    /** Estado al que se actualiza el pedido tras confirmar el pago. */
    private static final String ESTADO_PAGADO = "PAGADO";

    /** Tolerancia monetaria (€0.01) para comparar importes de tipo decimal. */
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");

    // ── Dependencias (instanciadas una sola vez, son thread-safe en modo lectura) ──
    private final PedidoDAO    pedidoDAO    = new PedidoDAO();
    private final UsuarioDAO   usuarioDAO   = new UsuarioDAO();
    private final EmailService emailService = new EmailService();

    // ══════════════════════════════════════════════════════════════════════════
    //  ÚNICO MÉTODO PÚBLICO: POST
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Punto de entrada del webhook.
     * bunq SIEMPRE espera un 200 OK; cualquier otro código provoca reintentos.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // ── 1. Respondemos 200 inmediatamente para no bloquear a bunq ──────────
        //    El procesamiento real ocurre ANTES de escribir la respuesta, pero
        //    dentro de un try-catch que garantiza que nunca propagamos un error.
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().print("{\"status\":\"received\"}");
        resp.getWriter().flush();

        // ── 2. Leer el cuerpo de la petición ──────────────────────────────────
        String body;
        try {
            body = HttpUtil.readBody(req);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "[BunqWebhook] No se pudo leer el body de la petición", e);
            return;
        }

        if (body == null || body.isBlank()) {
            LOG.warning("[BunqWebhook] Petición recibida con body vacío — ignorada.");
            return;
        }

        LOG.fine("[BunqWebhook] Payload recibido: " + body);

        // ── 3. Parsear JSON con Gson ───────────────────────────────────────────
        JsonObject root;
        try {
            root = JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            LOG.log(Level.WARNING, "[BunqWebhook] JSON inválido recibido de bunq", e);
            return;
        }

        // ── 4. Extraer datos del pago ──────────────────────────────────────────
        try {
            procesarPayload(root);
        } catch (Exception e) {
            // Capturamos cualquier error inesperado para no romper el webhook
            LOG.log(Level.SEVERE, "[BunqWebhook] Error inesperado durante el procesamiento", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE NEGOCIO PRINCIPAL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Extrae los datos del JSON de bunq y actualiza el pedido si procede.
     *
     * Estructura del JSON de bunq (simplificada):
     * <pre>
     * {
     *   "NotificationUrl": {
     *     "category": "PAYMENT",
     *     "object": {
     *       "Payment": {
     *         "amount": { "value": "49.99", "currency": "EUR" },
     *         "description": "Pago pedido SHINE-1234",
     *         "alias": { "display_name": "Juan García", "email": "juan@example.com" }
     *       }
     *     }
     *   }
     * }
     * </pre>
     */
    private void procesarPayload(JsonObject root) throws SQLException {

        // bunq encapsula el evento en NotificationUrl → object → Payment (o Mutation)
        JsonObject notif  = getNestedObject(root, "NotificationUrl");
        JsonObject object = getNestedObject(notif, "object");

        // El objeto puede llamarse "Payment" o "BunqMeTabResultInquiry", etc.
        // Intentamos primero con "Payment":
        JsonObject payment = null;
        if (object != null && object.has("Payment")) {
            payment = object.getAsJsonObject("Payment");
        } else if (object != null && object.has("MutationTabResultResponse")) {
            payment = object.getAsJsonObject("MutationTabResultResponse");
        }

        if (payment == null) {
            LOG.info("[BunqWebhook] Evento recibido sin objeto Payment reconocido — ignorado.");
            return;
        }

        // ── Extraer importe ──────────────────────────────────────────────────
        BigDecimal importe = null;
        JsonObject amountObj = getNestedObject(payment, "amount");
        if (amountObj != null && amountObj.has("value")) {
            try {
                importe = new BigDecimal(amountObj.get("value").getAsString())
                        .abs()                           // bunq puede enviar negativos en salidas
                        .setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                LOG.warning("[BunqWebhook] Importe no parseado: " + amountObj.get("value"));
            }
        }

        // ── Extraer descripción ───────────────────────────────────────────────
        String descripcion = getStringField(payment, "description");
        if (descripcion == null || descripcion.isBlank()) {
            LOG.info("[BunqWebhook] Pago sin descripción — no se puede identificar pedido.");
            return;
        }

        LOG.info("[BunqWebhook] Descripcion recibida: \"" + descripcion + "\" | Importe: " + importe);

        // ── Buscar patrón SHINE-XXXX con Regex ───────────────────────────────
        Matcher matcher = PEDIDO_PATTERN.matcher(descripcion.toUpperCase());
        if (!matcher.find()) {
            LOG.info("[BunqWebhook] No se encontró código de pedido SHINE-* en la descripción.");
            return;
        }

        String codigoCompleto = matcher.group(0);         // ej. "SHINE-1234"
        String codigoNumerico = matcher.group(1);         // ej. "1234"

        int idPedido;
        try {
            idPedido = Integer.parseInt(codigoNumerico);
        } catch (NumberFormatException e) {
            LOG.warning("[BunqWebhook] Código de pedido no numérico: " + codigoCompleto);
            return;
        }

        LOG.info("[BunqWebhook] Código de pedido identificado: " + codigoCompleto + " (id=" + idPedido + ")");

        // ── Consultar pedido en BD ────────────────────────────────────────────
        Optional<Pedido> optPedido;
        try {
            optPedido = pedidoDAO.findById(idPedido);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "[BunqWebhook] Error de BD al buscar pedido " + idPedido, e);
            throw e;  // se captura en doPost
        }

        if (optPedido.isEmpty()) {
            LOG.warning("[BunqWebhook] Pedido " + codigoCompleto + " no encontrado en la BD.");
            return;
        }

        Pedido pedido = optPedido.get();

        // ── Verificar estado ──────────────────────────────────────────────────
        if (!ESTADO_PENDIENTE.equalsIgnoreCase(pedido.getEstado())) {
            LOG.info("[BunqWebhook] Pedido " + codigoCompleto +
                    " ignorado — estado actual: \"" + pedido.getEstado() +
                    "\" (se esperaba \"" + ESTADO_PENDIENTE + "\")");
            return;
        }

        // ── Verificar importe ─────────────────────────────────────────────────
        if (importe == null) {
            LOG.warning("[BunqWebhook] No se pudo extraer el importe — no se actualiza el pedido.");
            return;
        }

        BigDecimal totalPedido = pedido.getTotal() != null
                ? pedido.getTotal().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        if (importe.subtract(totalPedido).abs().compareTo(TOLERANCIA) > 0) {
            LOG.warning(String.format(
                    "[BunqWebhook] Importe NO coincide para %s — recibido: %.2f €, esperado: %.2f €",
                    codigoCompleto, importe, totalPedido));
            return;
        }

        // ── ¡Todo correcto! → Actualizar estado a PAGADO ─────────────────────
        try {
            pedidoDAO.updateEstado(idPedido, ESTADO_PAGADO);
            LOG.info("[BunqWebhook] ✅ Pedido " + codigoCompleto + " actualizado a \"" + ESTADO_PAGADO + "\".");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "[BunqWebhook] Error de BD al actualizar pedido " + codigoCompleto, e);
            throw e;
        }

        // ── Enviar correo de factura ──────────────────────────────────────────
        enviarCorreoConfirmacion(pedido, importe);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENVÍO DE CORREO — obtenemos email/nombre del usuario
    // ══════════════════════════════════════════════════════════════════════════

    private void enviarCorreoConfirmacion(Pedido pedido, BigDecimal importe) {
        try {
            // El usuario del pedido solo tiene el id cargado; cargamos datos completos
            Optional<com.example.demo.entity.Usuario> optUsuario =
                    usuarioDAO.findById(pedido.getUsuario().getId());

            if (optUsuario.isEmpty()) {
                LOG.warning("[BunqWebhook] Usuario del pedido " + pedido.getIdPedido() + " no encontrado — email no enviado.");
                return;
            }

            Usuario usuario = optUsuario.get();
            emailService.enviarFacturaBizum(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    pedido.getIdPedido(),
                    importe
            );

        } catch (SQLException e) {
            LOG.log(Level.WARNING,
                    "[BunqWebhook] Error al obtener usuario para enviar email del pedido " + pedido.getIdPedido(), e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS DE PARSEO JSON (null-safe)
    // ══════════════════════════════════════════════════════════════════════════

    /** Devuelve el JsonObject anidado bajo la clave dada, o null si no existe. */
    private static JsonObject getNestedObject(JsonObject parent, String key) {
        if (parent == null || !parent.has(key)) return null;
        JsonElement el = parent.get(key);
        return el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    /** Devuelve el valor String del campo dado, o null si no existe/es null. */
    private static String getStringField(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return null;
        return obj.get(key).getAsString();
    }
}
