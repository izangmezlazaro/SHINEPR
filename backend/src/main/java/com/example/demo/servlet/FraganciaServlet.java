package com.example.demo.servlet;

import com.example.demo.dto.FraganciaDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.FraganciaService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET  /api/v1/fragancias        → listar
 * GET  /api/v1/fragancias/{id}   → obtener por id
 * POST /api/v1/fragancias        → crear
 */
@WebServlet(urlPatterns = "/api/v1/fragancias/*", name = "FraganciaServlet")
public class FraganciaServlet extends HttpServlet {

    private final FraganciaService fraganciaService = new FraganciaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                HttpUtil.writeJson(resp, 200, fraganciaService.listar());
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, fraganciaService.obtenerPorId(id));
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
            FraganciaDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), FraganciaDTO.class);
            HttpUtil.writeJson(resp, 201, fraganciaService.crear(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
