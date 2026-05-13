package com.example.demo.servlet;

import com.example.demo.dto.NotaOlfativaDTO;
import com.example.demo.dao.NotaOlfativaDAO;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.util.HttpUtil;
import com.example.demo.util.JsonUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GET /api/v1/notas-olfativas                    → listar todas
 * GET /api/v1/notas-olfativas/fragancia/{id}      → listar por fragancia
 */
@WebServlet(urlPatterns = "/api/v1/notas-olfativas/*", name = "NotaOlfativaServlet")
public class NotaOlfativaServlet extends HttpServlet {

    private final NotaOlfativaDAO notaOlfativaDAO = new NotaOlfativaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path != null && path.startsWith("/fragancia/")) {
                int idFragancia = HttpUtil.extractId(path.substring("/fragancia".length()));
                List<NotaOlfativaDTO> dtos = notaOlfativaDAO.findByFraganciaId(idFragancia).stream()
                        .map(n -> new NotaOlfativaDTO(n.getIdNota(), n.getFragancia().getIdFragancia(),
                                n.getNombre(), n.getTipo(), n.getUrlImagen()))
                        .collect(Collectors.toList());
                HttpUtil.writeJson(resp, 200, dtos);
            } else {
                List<NotaOlfativaDTO> dtos = notaOlfativaDAO.findAll().stream()
                        .map(n -> new NotaOlfativaDTO(n.getIdNota(), n.getFragancia().getIdFragancia(),
                                n.getNombre(), n.getTipo(), n.getUrlImagen()))
                        .collect(Collectors.toList());
                HttpUtil.writeJson(resp, 200, dtos);
            }
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (SQLException e) {
            HttpUtil.writeError(resp, 500, "Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
