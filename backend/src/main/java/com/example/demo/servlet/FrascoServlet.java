package com.example.demo.servlet;

import com.example.demo.dto.FrascoDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.FrascoService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * GET  /api/v1/frascos        → listar
 * GET  /api/v1/frascos/{id}   → obtener por id
 * POST /api/v1/frascos        → crear
 */
@WebServlet(urlPatterns = "/api/v1/frascos/*", name = "FrascoServlet")
public class FrascoServlet extends HttpServlet {

    private final FrascoService frascoService = new FrascoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                HttpUtil.writeJson(resp, 200, frascoService.listar());
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, frascoService.obtenerPorId(id));
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
            FrascoDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), FrascoDTO.class);
            HttpUtil.writeJson(resp, 201, frascoService.crear(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
