package com.example.demo.servlet;

import com.example.demo.dto.CategoriaDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.CategoriaService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET  /api/v1/categorias        → listar
 * GET  /api/v1/categorias/{id}   → obtener por id
 * POST /api/v1/categorias        → crear (admin)
 */
@WebServlet(urlPatterns = "/api/v1/categorias/*", name = "CategoriaServlet")
public class CategoriaServlet extends HttpServlet {

    private final CategoriaService categoriaService = new CategoriaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                HttpUtil.writeJson(resp, 200, categoriaService.listar());
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, categoriaService.obtenerPorId(id));
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
            CategoriaDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), CategoriaDTO.class);
            HttpUtil.writeJson(resp, 201, categoriaService.crear(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
