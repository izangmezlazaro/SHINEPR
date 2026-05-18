package com.example.demo.servlet;

import com.example.demo.dto.WebhookPayloadDTO;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.PagoService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/v1/webhook/bunq
 *
 * Make.com llama a este endpoint cuando detecta un email de pago confirmado
 * de bunq. Requiere la cabecera X-Webhook-Secret con el valor correcto.
 *
 * Body JSON esperado: { "idPedido": 123 }
 */
@WebServlet(urlPatterns = "/api/v1/webhook/bunq", name = "WebhookBunqServlet")
public class WebhookBunqServlet extends HttpServlet {

    // Cambiar este valor y usar el mismo en Make.com como cabecera X-Webhook-Secret
    private static final String WEBHOOK_SECRET = "shine-bunq-secret-2024";

    private final PagoService pagoService = new PagoService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String secret = req.getHeader("X-Webhook-Secret");
        if (!WEBHOOK_SECRET.equals(secret)) {
            HttpUtil.writeError(resp, 401, "Secreto de webhook inválido");
            return;
        }

        try {
            WebhookPayloadDTO payload = JsonUtil.fromJson(HttpUtil.readBody(req), WebhookPayloadDTO.class);
            if (payload.getIdPedido() == null) {
                HttpUtil.writeError(resp, 400, "El campo idPedido es obligatorio");
                return;
            }
            pagoService.confirmarPagoPorPedido(payload.getIdPedido());
            HttpUtil.writeJson(resp, 200, java.util.Map.of(
                    "mensaje", "Pago confirmado correctamente",
                    "idPedido", payload.getIdPedido()
            ));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
