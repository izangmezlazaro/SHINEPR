package com.example.demo.servlet;

import com.example.demo.dto.CarritoItemRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.CarritoService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET    /api/v1/carrito             → obtener o crear carrito del usuario autenticado
 * POST   /api/v1/carrito/items       → agregar ítem
 * DELETE /api/v1/carrito/items/{id}  → eliminar ítem
 * DELETE /api/v1/carrito             → vaciar carrito
 */
@WebServlet(urlPatterns = "/api/v1/carrito/*", name = "CarritoServlet")
public class CarritoServlet extends HttpServlet {

    private final CarritoService carritoService = new CarritoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            HttpUtil.writeJson(resp, 200, carritoService.obtenerOCrearPorUsuario(userId));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            CarritoItemRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), CarritoItemRequestDTO.class);
            HttpUtil.writeJson(resp, 201, carritoService.agregarItem(userId, dto));
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
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            String path = req.getPathInfo();
            if (path == null || !path.startsWith("/items/")) {
                HttpUtil.writeError(resp, 400, "Ruta no válida"); return;
            }
            int idItem = HttpUtil.extractId(path.substring("/items".length()));
            CarritoItemRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), CarritoItemRequestDTO.class);
            if (dto.getCantidad() == null || dto.getCantidad() < 1) {
                HttpUtil.writeError(resp, 400, "La cantidad debe ser al menos 1"); return;
            }
            HttpUtil.writeJson(resp, 200, carritoService.actualizarCantidad(userId, idItem, dto.getCantidad()));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            String path = req.getPathInfo();
            if (path != null && path.startsWith("/items/")) {
                // DELETE /api/v1/carrito/items/{idItem}
                int idItem = HttpUtil.extractId(path.substring("/items".length()));
                carritoService.eliminarItem(userId, idItem);
            } else {
                // DELETE /api/v1/carrito  → vaciar todo
                carritoService.vaciar(userId);
            }
            resp.setStatus(204);
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
