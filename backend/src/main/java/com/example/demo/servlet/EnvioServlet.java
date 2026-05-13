package com.example.demo.servlet;

import com.example.demo.dto.EnvioDTO;
import com.example.demo.dto.EstadoEnvioRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.EnvioService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET   /api/v1/envios/pedido/{idPedido}  → obtener envío del pedido
 * POST  /api/v1/envios                    → crear envío
 * PUT   /api/v1/envios/{id}/estado        → actualizar estado
 */
@WebServlet(urlPatterns = "/api/v1/envios/*", name = "EnvioServlet")
public class EnvioServlet extends HttpServlet {

    private final EnvioService envioService = new EnvioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo(); // "/pedido/123"
            if (path != null && path.startsWith("/pedido/")) {
                int idPedido = HttpUtil.extractId(path.substring("/pedido".length()));
                HttpUtil.writeJson(resp, 200, envioService.obtenerPorPedido(idPedido));
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
            EnvioDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), EnvioDTO.class);
            HttpUtil.writeJson(resp, 201, envioService.crear(dto));
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
            // PUT /api/v1/envios/{id}/estado
            String path = req.getPathInfo();
            if (path == null || !path.endsWith("/estado")) {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada"); return;
            }
            // Extraer id: "/123/estado" → "123"
            String withoutEstado = path.substring(0, path.lastIndexOf("/estado"));
            int id = HttpUtil.extractId(withoutEstado);
            EstadoEnvioRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), EstadoEnvioRequestDTO.class);
            HttpUtil.writeJson(resp, 200, envioService.actualizarEstado(id, dto));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
