package com.example.demo.servlet;

import com.example.demo.service.PedidoService;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * GET /api/v1/intranet/pedidos          → lista todos los pedidos (admin)
 * PUT /api/v1/intranet/pedidos/{id}/estado → actualiza el estado de un pedido
 */
@WebServlet(urlPatterns = "/api/v1/intranet/pedidos/*", name = "IntranetPedidoServlet")
public class IntranetPedidoServlet extends HttpServlet {

    private static final Set<String> ESTADOS_VALIDOS =
        Set.of("pendiente", "pendiente_bizum", "procesando", "enviado", "entregado", "cancelado");

    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, pedidoService.listarTodosAdmin());
        } catch (Exception e) {
            System.err.println("[IntranetPedidoServlet] ERROR: " + e);
            e.printStackTrace(System.err);
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    /**
     * PUT /api/v1/intranet/pedidos/{id}/estado
     * Body: { "estado": "enviado" }
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo(); // e.g. "/42/estado"
            if (path == null || !path.contains("/")) {
                HttpUtil.writeError(resp, 400, "Ruta inválida. Use /intranet/pedidos/{id}/estado");
                return;
            }
            String[] parts = path.split("/");
            if (parts.length < 2) {
                HttpUtil.writeError(resp, 400, "Falta el ID del pedido");
                return;
            }
            int idPedido = Integer.parseInt(parts[1]);

            JsonObject body  = JsonParser.parseString(HttpUtil.readBody(req)).getAsJsonObject();
            String    estado = body.has("estado") ? body.get("estado").getAsString().toLowerCase().trim() : null;

            if (estado == null || !ESTADOS_VALIDOS.contains(estado)) {
                HttpUtil.writeError(resp, 400, "Estado inválido. Valores permitidos: " + ESTADOS_VALIDOS);
                return;
            }

            pedidoService.actualizarEstado(idPedido, estado);
            HttpUtil.writeJson(resp, 200, new StatusResponse(idPedido, estado));
        } catch (NumberFormatException e) {
            HttpUtil.writeError(resp, 400, "ID de pedido no válido");
        } catch (Exception e) {
            System.err.println("[IntranetPedidoServlet.doPut] ERROR: " + e);
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    private static final class StatusResponse {
        @SuppressWarnings("unused") final int    idPedido;
        @SuppressWarnings("unused") final String estado;
        StatusResponse(int idPedido, String estado) { this.idPedido = idPedido; this.estado = estado; }
    }
}
