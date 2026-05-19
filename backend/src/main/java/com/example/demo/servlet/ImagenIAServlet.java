package com.example.demo.servlet;

import com.example.demo.service.OpenAIService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * POST /api/v1/ia/imagen
 * Body: { "prompt": "descripción de la imagen" }
 * Response: { "url": "https://..." }
 *
 * Requiere JWT (usuario autenticado). Llama a DALL·E 3 de OpenAI.
 */
public class ImagenIAServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final OpenAIService openAIService = new OpenAIService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {
            String rawBody = req.getReader().lines().collect(Collectors.joining());
            JsonObject json = GSON.fromJson(rawBody, JsonObject.class);

            if (json == null || !json.has("prompt") || json.get("prompt").getAsString().isBlank()) {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "El campo 'prompt' es requerido");
                return;
            }

            String prompt = json.get("prompt").getAsString().trim();
            String imageUrl = openAIService.generarImagen(prompt);

            JsonObject result = new JsonObject();
            result.addProperty("url", imageUrl);
            writeJson(resp, HttpServletResponse.SC_OK, result);

        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error al generar imagen: " + e.getMessage());
        }
    }

    private void writeJson(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        try (PrintWriter pw = resp.getWriter()) {
            pw.print(GSON.toJson(body));
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        JsonObject err = new JsonObject();
        err.addProperty("error", message);
        writeJson(resp, status, err);
    }
}
