package com.example.demo.servlet;

import com.example.demo.dto.DireccionDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.DireccionService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET    /api/v1/direcciones           → listar por usuario autenticado
 * POST   /api/v1/direcciones           → crear
 * PUT    /api/v1/direcciones/{id}      → actualizar
 * DELETE /api/v1/direcciones/{id}      → eliminar
 */
@WebServlet(urlPatterns = "/api/v1/direcciones/*", name = "DireccionServlet")
public class DireccionServlet extends HttpServlet {

    private final DireccionService direccionService = new DireccionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            HttpUtil.writeJson(resp, 200, direccionService.listarPorUsuario(userId));
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            DireccionDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), DireccionDTO.class);
            HttpUtil.writeJson(resp, 201, direccionService.crear(dto));
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
            DireccionDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), DireccionDTO.class);
            HttpUtil.writeJson(resp, 200, direccionService.actualizar(id, dto));
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
            int id = HttpUtil.extractId(req.getPathInfo());
            direccionService.eliminar(id);
            resp.setStatus(204);
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
