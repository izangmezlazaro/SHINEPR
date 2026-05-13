package com.example.demo.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * Métodos estáticos de utilidad para Servlets:
 * - Escribir respuestas JSON
 * - Leer el body de la petición
 * - Obtener el userId inyectado por JwtFilter
 */
public final class HttpUtil {

    private HttpUtil() {}

    /**
     * Escribe una respuesta JSON con el código de estado indicado.
     */
    public static void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter pw = resp.getWriter()) {
            pw.print(JsonUtil.toJson(body));
        }
    }

    /**
     * Escribe una respuesta de error JSON estándar: {"error": "mensaje"}
     */
    public static void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeJson(resp, status, new ErrorBody(message));
    }

    /**
     * Lee el body completo de la petición HTTP como String.
     */
    public static String readBody(HttpServletRequest req) throws IOException {
        try (BufferedReader reader = req.getReader()) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    /**
     * Obtiene el userId inyectado por JwtFilter como atributo de la petición.
     * Devuelve null si no hay usuario autenticado.
     */
    public static Integer getAuthUserId(HttpServletRequest req) {
        return (Integer) req.getAttribute("userId");
    }

    /**
     * Obtiene el rol del usuario autenticado inyectado por JwtFilter.
     */
    public static String getAuthRol(HttpServletRequest req) {
        return (String) req.getAttribute("rol");
    }

    /**
     * Obtiene el último segmento de pathInfo: "/123" → 123
     * Lanza NumberFormatException si no es un número.
     */
    public static int extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new IllegalArgumentException("Se requiere un ID en la ruta");
        }
        String segment = pathInfo.substring(1); // quita el "/"
        int slash = segment.indexOf('/');
        if (slash >= 0) segment = segment.substring(0, slash);
        return Integer.parseInt(segment);
    }

    // ── Clase interna para body de error ────────────────────────────────────
    private static final class ErrorBody {
        @SuppressWarnings("unused")
        private final String error;
        ErrorBody(String error) { this.error = error; }
    }
}
