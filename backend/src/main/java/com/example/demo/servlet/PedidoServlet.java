package com.example.demo.servlet;

import com.example.demo.dto.PedidoRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.PedidoService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET  /api/v1/pedidos        → listar pedidos del usuario autenticado
 * GET  /api/v1/pedidos/{id}   → obtener pedido por id
 * POST /api/v1/pedidos        → crear pedido desde carrito
 */
@WebServlet(urlPatterns = "/api/v1/pedidos/*", name = "PedidoServlet")
public class PedidoServlet extends HttpServlet {

    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                HttpUtil.writeJson(resp, 200, pedidoService.listarPorUsuario(userId));
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, pedidoService.obtenerPorId(id));
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
            PedidoRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), PedidoRequestDTO.class);
            HttpUtil.writeJson(resp, 201, pedidoService.crearDesdeCarrito(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
