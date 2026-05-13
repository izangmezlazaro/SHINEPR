package com.example.demo.servlet;

import com.example.demo.dto.ProductoRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.ProductoService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET    /api/v1/productos         → listar todos
 * GET    /api/v1/productos/{id}    → obtener por id
 * POST   /api/v1/productos         → crear
 * PUT    /api/v1/productos/{id}    → actualizar
 * DELETE /api/v1/productos/{id}    → eliminar
 * POST   /api/v1/productos/upsert  → upsert por SKU
 */
@WebServlet(urlPatterns = "/api/v1/productos/*", name = "ProductoServlet")
public class ProductoServlet extends HttpServlet {

    private final ProductoService productoService = new ProductoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                HttpUtil.writeJson(resp, 200, productoService.listar());
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, productoService.obtenerPorId(id));
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
            String path = req.getPathInfo();
            ProductoRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), ProductoRequestDTO.class);
            if ("/upsert".equals(path)) {
                HttpUtil.writeJson(resp, 200, productoService.upsertPorSku(dto));
            } else {
                HttpUtil.writeJson(resp, 201, productoService.crear(dto));
            }
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
            int id = HttpUtil.extractId(req.getPathInfo());
            ProductoRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), ProductoRequestDTO.class);
            HttpUtil.writeJson(resp, 200, productoService.actualizar(id, dto));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = HttpUtil.extractId(req.getPathInfo());
            productoService.eliminar(id);
            resp.setStatus(204);
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
