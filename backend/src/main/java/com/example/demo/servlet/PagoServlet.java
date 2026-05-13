package com.example.demo.servlet;

import com.example.demo.dto.EstadoPagoRequestDTO;
import com.example.demo.dto.PagoDTO;
import com.example.demo.exception.BadRequestException;
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
 * GET  /api/v1/pagos/pedido/{idPedido}  → obtener pago del pedido
 * POST /api/v1/pagos                    → crear pago
 * PUT  /api/v1/pagos/{id}/estado        → actualizar estado del pago
 */
@WebServlet(urlPatterns = "/api/v1/pagos/*", name = "PagoServlet")
public class PagoServlet extends HttpServlet {

    private final PagoService pagoService = new PagoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path != null && path.startsWith("/pedido/")) {
                int idPedido = HttpUtil.extractId(path.substring("/pedido".length()));
                HttpUtil.writeJson(resp, 200, pagoService.obtenerPorPedido(idPedido));
            } else {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada");
            }
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            PagoDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), PagoDTO.class);
            HttpUtil.writeJson(resp, 201, pagoService.crear(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || !path.endsWith("/estado")) {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada"); return;
            }
            String withoutEstado = path.substring(0, path.lastIndexOf("/estado"));
            int id = HttpUtil.extractId(withoutEstado);
            EstadoPagoRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), EstadoPagoRequestDTO.class);
            HttpUtil.writeJson(resp, 200, pagoService.actualizarEstado(id, dto.getEstado()));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
