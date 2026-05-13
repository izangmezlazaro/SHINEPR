package com.example.demo.servlet;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.service.AnuncioService;
import com.example.demo.util.HttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET    /api/v1/anuncios       → listar todos
 * POST   /api/v1/anuncios       → crear anuncio
 * DELETE /api/v1/anuncios/{id}  → eliminar anuncio
 */
@WebServlet(urlPatterns = "/api/v1/anuncios/*", name = "AnuncioServlet")
public class AnuncioServlet extends HttpServlet {

    private final AnuncioService anuncioService = new AnuncioService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, anuncioService.listar());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonParser.parseString(HttpUtil.readBody(req)).getAsJsonObject();
            String titulo    = body.has("titulo")    ? body.get("titulo").getAsString()    : null;
            String tag       = body.has("tag")       ? body.get("tag").getAsString()       : null;
            String tagLabel  = body.has("tagLabel")  ? body.get("tagLabel").getAsString()  : null;
            String mensaje   = body.has("mensaje")   ? body.get("mensaje").getAsString()   : null;
            String autor     = body.has("autor")     ? body.get("autor").getAsString()     : null;
            HttpUtil.writeJson(resp, 201, anuncioService.crear(titulo, tag, tagLabel, mensaje, autor));
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
            anuncioService.eliminar(id);
            HttpUtil.writeJson(resp, 200, "{\"ok\":true}");
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
