package com.example.demo.servlet;

import com.example.demo.service.SubcategoriaService;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/v1/subcategorias               → todas las subcategorías
 * GET /api/v1/subcategorias?categoriaId=X → subcategorías de una categoría
 */
@WebServlet(urlPatterns = "/api/v1/subcategorias/*", name = "SubcategoriaServlet")
public class SubcategoriaServlet extends HttpServlet {

    private final SubcategoriaService subcategoriaService = new SubcategoriaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String catIdParam = req.getParameter("categoriaId");
            if (catIdParam != null && !catIdParam.isBlank()) {
                int catId = Integer.parseInt(catIdParam.trim());
                HttpUtil.writeJson(resp, 200, subcategoriaService.listarPorCategoria(catId));
            } else {
                HttpUtil.writeJson(resp, 200, subcategoriaService.listar());
            }
        } catch (NumberFormatException e) {
            HttpUtil.writeError(resp, 400, "categoriaId debe ser un número entero");
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
