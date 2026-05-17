package com.example.demo.servlet;

import com.example.demo.entity.BuzonMensaje;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.BuzonService;
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
 * GET  /api/v1/buzon   → listar todos los mensajes (admin)
 * POST /api/v1/buzon   → enviar un nuevo mensaje (empleado)
 */
@WebServlet(urlPatterns = "/api/v1/buzon/*", name = "BuzonServlet")
public class BuzonServlet extends HttpServlet {

    private final BuzonService buzonService = new BuzonService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<BuzonMensaje> mensajes = buzonService.listar();
            HttpUtil.writeJson(resp, 200, mensajes);
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject body = JsonParser.parseString(HttpUtil.readBody(req)).getAsJsonObject();
            String nombre    = body.has("nombre")    ? body.get("nombre").getAsString()    : null;
            String apellidos = body.has("apellidos") ? body.get("apellidos").getAsString() : null;
            String asunto    = body.has("asunto")    ? body.get("asunto").getAsString()    : null;
            String texto     = body.has("texto")     ? body.get("texto").getAsString()     : "";
            BuzonMensaje guardado = buzonService.enviar(nombre, apellidos, asunto, texto);
            HttpUtil.writeJson(resp, 201, guardado);
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
