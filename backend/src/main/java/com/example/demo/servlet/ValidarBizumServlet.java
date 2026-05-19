package com.example.demo.servlet;

import com.example.demo.dao.PagoDAO;
import com.example.demo.dao.PedidoDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.entity.Pago;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;
import com.example.demo.util.EmailUtil;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * PUT /api/v1/intranet/validar-bizum
 * Body: { "idPedido": 42 }
 *
 * Flujo:
 *  1. Actualiza el estado del pedido de 'pendiente_bizum' → 'procesando'
 *  2. Actualiza el pago asociado a 'completado' y registra la fecha de pago
 *  3. Envía email de confirmación al cliente
 */
@WebServlet(urlPatterns = "/api/v1/intranet/validar-bizum", name = "ValidarBizumServlet")
public class ValidarBizumServlet extends HttpServlet {

    private final PedidoDAO  pedidoDAO  = new PedidoDAO();
    private final PagoDAO    pagoDAO    = new PagoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String body = HttpUtil.readBody(req);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            if (!json.has("idPedido") || json.get("idPedido").isJsonNull()) {
                HttpUtil.writeError(resp, 400, "El campo 'idPedido' es obligatorio");
                return;
            }

            int idPedido = json.get("idPedido").getAsInt();

            try (Connection conn = ConexionDB.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // ── 1. Verificar estado actual ──────────────────
                    String estadoActual = pedidoDAO.getEstadoActual(idPedido, conn);
                    if (estadoActual == null) {
                        HttpUtil.writeError(resp, 404, "Pedido #" + idPedido + " no encontrado");
                        conn.rollback();
                        return;
                    }
                    if (!"pendiente_bizum".equalsIgnoreCase(estadoActual)) {
                        HttpUtil.writeError(resp, 409,
                            "El pedido #" + idPedido + " no está en estado 'pendiente_bizum'. Estado actual: " + estadoActual);
                        conn.rollback();
                        return;
                    }

                    // ── 2. Actualizar estado del pedido → 'procesando' ──
                    pedidoDAO.updateEstado(idPedido, "procesando", conn);

                    // ── 2.5 Descontar stock ──
                    try (var ps = conn.prepareStatement("SELECT id_producto, cantidad FROM detalle_pedido WHERE id_pedido = ? AND id_producto IS NOT NULL")) {
                        ps.setInt(1, idPedido);
                        try (var rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int prodId = rs.getInt("id_producto");
                                int cant = rs.getInt("cantidad");
                                try (var updatePs = conn.prepareStatement("UPDATE producto SET stock = stock - ? WHERE id_producto = ?")) {
                                    updatePs.setInt(1, cant);
                                    updatePs.setInt(2, prodId);
                                    updatePs.executeUpdate();
                                }
                            }
                        }
                    }

                    // ── 3. Actualizar/crear registro de pago → 'completado' ──
                    Pago pago = pagoDAO.findByPedidoId(idPedido).orElse(null);
                    if (pago != null) {
                        pago.setEstado("completado");
                        pago.setFechaPago(LocalDateTime.now());
                        // Actualizar directamente con SQL dentro de la transacción
                        try (var ps = conn.prepareStatement(
                                "UPDATE pago SET estado = 'completado', fecha_pago = ? WHERE id_pedido = ?")) {
                            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                            ps.setInt(2, idPedido);
                            ps.executeUpdate();
                        }
                    } else {
                        // Crear registro de pago si no existe
                        try (var ps = conn.prepareStatement(
                                "INSERT INTO pago (id_pedido, metodo_pago, fecha_pago, estado) VALUES (?, 'bizum', ?, 'completado')")) {
                            ps.setInt(1, idPedido);
                            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                            ps.executeUpdate();
                        }
                    }

                    conn.commit();

                    // ── 4. Obtener datos del cliente para el email ──────────
                    Pedido pedido = pedidoDAO.findById(idPedido).orElse(null);
                    String emailCliente   = "—";
                    String nombreCliente  = "Cliente";
                    String totalStr       = "0.00";

                    if (pedido != null) {
                        if (pedido.getTotal() != null) {
                            totalStr = pedido.getTotal().toPlainString();
                        }
                        try {
                            // Buscar email del usuario asociado al pedido
                            String[] datos = fetchClienteData(pedido.getUsuario().getId());
                            nombreCliente = datos[0];
                            emailCliente  = datos[1];
                        } catch (Exception ex) {
                            System.err.println("[ValidarBizumServlet] No se pudieron obtener datos del cliente: " + ex.getMessage());
                        }
                    }

                    // ── 5. Enviar email de confirmación ─────────────────────
                    // Ejecutamos en hilo separado para no bloquear la respuesta HTTP
                    final String emailFinal   = emailCliente;
                    final String nombreFinal  = nombreCliente;
                    final String totalFinal   = totalStr;
                    final int    pedidoIdFinal = idPedido;

                    Thread emailThread = new Thread(() ->
                        EmailUtil.enviarConfirmacionBizum(emailFinal, nombreFinal, pedidoIdFinal, totalFinal)
                    );
                    emailThread.setDaemon(true);
                    emailThread.start();

                    // ── 6. Responder OK ─────────────────────────────────────
                    JsonObject result = new JsonObject();
                    result.addProperty("ok", true);
                    result.addProperty("idPedido", idPedido);
                    result.addProperty("nuevoEstado", "procesando");
                    result.addProperty("emailEnviado", !emailCliente.equals("—"));
                    result.addProperty("mensaje", "Pago Bizum validado correctamente. Email de confirmación enviado a " + emailCliente);
                    HttpUtil.writeJson(resp, 200, result);

                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                }
            }

        } catch (Exception e) {
            System.err.println("[ValidarBizumServlet] ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    /** Obtiene [nombre, email] del cliente por su userId. */
    private String[] fetchClienteData(int userId) throws Exception {
        try (Connection conn = ConexionDB.getConnection();
             var ps = conn.prepareStatement("SELECT nombre, email FROM usuario WHERE id = ?")) {
            ps.setInt(1, userId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{ rs.getString("nombre"), rs.getString("email") };
                }
            }
        }
        return new String[]{ "Cliente", "—" };
    }
}
