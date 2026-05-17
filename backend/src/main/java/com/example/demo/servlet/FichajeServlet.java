package com.example.demo.servlet;

import com.example.demo.entity.Fichaje;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.FichajeService;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * GET  /api/v1/fichajes          → listar todos (admin)
 * GET  /api/v1/fichajes?fecha=   → listar por fecha YYYY-MM-DD
 * POST /api/v1/fichajes          → registrar entrada o salida
 *   body: { "empleadoEmail": "...", "empleadoNombre": "...", "tipo": "ENTRADA"|"SALIDA" }
 */
@WebServlet(urlPatterns = "/api/v1/fichajes/*", name = "FichajeServlet")
public class FichajeServlet extends HttpServlet {

    private final FichajeService fichajeService = new FichajeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String fecha = req.getParameter("fecha");
            List<Fichaje> list = (fecha != null && !fecha.isBlank())
                    ? fichajeService.listarPorFecha(fecha)
                    : fichajeService.listarTodos();
            HttpUtil.writeJson(resp, 200, list);
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body    = JsonParser.parseString(HttpUtil.readBody(req)).getAsJsonObject();
            String email       = body.has("empleadoEmail")  ? body.get("empleadoEmail").getAsString()  : null;
            String nombre      = body.has("empleadoNombre") ? body.get("empleadoNombre").getAsString() : null;
            String tipo        = body.has("tipo")           ? body.get("tipo").getAsString()           : null;
            Fichaje guardado   = fichajeService.registrar(email, nombre, tipo);
            HttpUtil.writeJson(resp, 201, guardado);
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
