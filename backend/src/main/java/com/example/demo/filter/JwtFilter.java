package com.example.demo.filter;

import com.example.demo.security.JwtUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Filtro JWT que protege las rutas privadas de la API.
 * Si el token es válido, inyecta userId y rol como atributos en el request.
 *
 * Rutas públicas (whitelist):
 *  - POST /api/v1/auth/*
 *  - GET  /api/v1/productos/*
 *  - GET  /api/v1/categorias/*
 *  - GET  /api/v1/fragancias/*
 *  - GET  /api/v1/frascos/*
 *  - GET  /api/v1/notas-olfativas/*
 */
@WebFilter(urlPatterns = "/api/*", filterName = "JwtFilter")
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil = JwtUtil.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isPublicRoute(req)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(resp, "Token de autenticación requerido");
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            sendUnauthorized(resp, "Token inválido o expirado");
            return;
        }

        // Inyectar datos del usuario en el request para uso en Servlets
        req.setAttribute("userId", jwtUtil.getUserId(token));
        req.setAttribute("email",  jwtUtil.getEmail(token));
        req.setAttribute("rol",    jwtUtil.getRol(token));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    /**
     * Determina si la ruta/método es pública (no requiere JWT).
     */
    private boolean isPublicRoute(HttpServletRequest req) {
        String method = req.getMethod();
        String path   = req.getRequestURI();

        // Auth siempre público
        if (path.startsWith("/api/v1/auth/")) return true;

        // Catálogo de solo lectura — público
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/v1/productos"))       return true;
            if (path.startsWith("/api/v1/categorias"))      return true;
            if (path.startsWith("/api/v1/fragancias"))       return true;
            if (path.startsWith("/api/v1/frascos"))          return true;
            if (path.startsWith("/api/v1/notas-olfativas")) return true;
        }

        // Intranet — autenticación gestionada en cliente (staff login hardcoded)
        if (path.startsWith("/api/v1/fichajes"))  return true;
        if (path.startsWith("/api/v1/anuncios"))  return true;
        if (path.startsWith("/api/v1/reuniones")) return true;

        return false;
    }

    private void sendUnauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter pw = resp.getWriter()) {
            pw.print("{\"error\":\"" + message + "\"}");
        }
    }
}
