package com.example.demo.filter;

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

/**
 * Filtro CORS global. Se ejecuta en todas las rutas ("/*").
 * Permite los orígenes del frontend (Live Server) y maneja preflight OPTIONS.
 */
@WebFilter(urlPatterns = "/*", filterName = "CorsFilter")
public class CorsFilter implements Filter {

    private static final String[] ALLOWED_ORIGINS = {
        "http://127.0.0.1:5500",
        "http://127.0.0.1:5501",
        "http://127.0.0.1:5502",
        "http://localhost:5500",
        "http://localhost:5501",
        "http://localhost:5502"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String origin = req.getHeader("Origin");

        if (isAllowedOrigin(origin)) {
            resp.setHeader("Access-Control-Allow-Origin",      origin);
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Access-Control-Allow-Methods",     "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            resp.setHeader("Access-Control-Allow-Headers",     "Content-Type, Authorization");
            resp.setHeader("Access-Control-Expose-Headers",    "Authorization");
            resp.setHeader("Vary",                             "Origin");
        }

        // Preflight OPTIONS → responder 200 directamente sin pasar al Servlet
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}

    private boolean isAllowedOrigin(String origin) {
        if (origin == null) return false;
        for (String allowed : ALLOWED_ORIGINS) {
            if (allowed.equals(origin)) return true;
        }
        return false;
    }
}
