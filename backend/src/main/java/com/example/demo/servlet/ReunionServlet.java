package com.example.demo.servlet;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.ReunionService;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET    /api/v1/reuniones       → listar todas
 * POST   /api/v1/reuniones       → crear reunión
 * DELETE /api/v1/reuniones/{id}  → eliminar reunión
 */
@WebServlet(urlPatterns = "/api/v1/reuniones/*", name = "ReunionServlet")
public class ReunionServlet extends HttpServlet {

    private final ReunionService reunionService = new ReunionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, reunionService.listar());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonParser.parseString(HttpUtil.readBody(req)).getAsJsonObject();
            String titulo     = body.has("titulo")     ? body.get("titulo").getAsString()     : null;
            String fecha      = body.has("fecha")      ? body.get("fecha").getAsString()      : null;
            String hora       = body.has("hora")       ? body.get("hora").getAsString()       : null;
            String plataforma = body.has("plataforma") ? body.get("plataforma").getAsString() : null;
            String asistentes = body.has("attendees")  ? body.get("attendees").getAsString()  :
                                body.has("asistentes") ? body.get("asistentes").getAsString() : null;
            String color      = body.has("color")      ? body.get("color").getAsString()      : "rose";
            String creadoPor  = body.has("creadoPor")  ? body.get("creadoPor").getAsString()  : null;
            HttpUtil.writeJson(resp, 201, reunionService.crear(titulo, fecha, hora, plataforma, asistentes, color, creadoPor));
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = HttpUtil.extractId(req.getPathInfo());
            reunionService.eliminar(id);
            HttpUtil.writeJson(resp, 200, "{\"ok\":true}");
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
