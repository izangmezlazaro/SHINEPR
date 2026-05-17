package com.example.demo.servlet;

import com.example.demo.dao.ActividadDAO;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/v1/intranet/actividad → feed de actividad reciente agregado
 * de múltiples tablas (pedidos, fichajes, anuncios).
 * Diseñado para ser escalable: nuevos tipos de evento se añaden en ActividadDAO
 * sin modificar este servlet ni el frontend.
 */
@WebServlet(urlPatterns = "/api/v1/intranet/actividad/*", name = "IntranetActividadServlet")
public class IntranetActividadServlet extends HttpServlet {

    private final ActividadDAO actividadDAO = new ActividadDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, actividadDAO.getActividadReciente());
        } catch (Exception e) {
            System.err.println("[IntranetActividadServlet] ERROR: " + e);
            e.printStackTrace(System.err);
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
