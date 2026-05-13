package com.example.demo.servlet;

import com.example.demo.dto.PerfumeCustomRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.PerfumeCustomService;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET  /api/v1/perfumes-custom              → listar por usuario autenticado
 * GET  /api/v1/perfumes-custom/{id}         → obtener por id
 * POST /api/v1/perfumes-custom              → crear
 */
@WebServlet(urlPatterns = "/api/v1/perfumes-custom/*", name = "PerfumeCustomServlet")
public class PerfumeCustomServlet extends HttpServlet {

    private final PerfumeCustomService perfumeCustomService = new PerfumeCustomService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/") || path.isEmpty()) {
                Integer userId = HttpUtil.getAuthUserId(req);
                if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
                HttpUtil.writeJson(resp, 200, perfumeCustomService.listarPorUsuario(userId));
            } else {
                int id = HttpUtil.extractId(path);
                HttpUtil.writeJson(resp, 200, perfumeCustomService.obtenerPorId(id));
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
            Integer userId = HttpUtil.getAuthUserId(req);
            if (userId == null) { HttpUtil.writeError(resp, 401, "No autenticado"); return; }
            PerfumeCustomRequestDTO dto = JsonUtil.fromJson(HttpUtil.readBody(req), PerfumeCustomRequestDTO.class);
            dto.setIdUsuario(userId);
            HttpUtil.writeJson(resp, 201, perfumeCustomService.crear(dto));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
