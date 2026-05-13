package com.example.demo.servlet;

import com.example.demo.dao.UsuarioDAO;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * GET /api/v1/usuarios/{id}/puntos → devuelve los puntos de fidelidad del usuario
 */
@WebServlet(urlPatterns = "/api/v1/usuarios/*", name = "UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || !path.endsWith("/puntos")) {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada");
                return;
            }
            String withoutPuntos = path.substring(0, path.lastIndexOf("/puntos"));
            int idUsuario = HttpUtil.extractId(withoutPuntos);
            Usuario usuario = usuarioDAO.findById(idUsuario)
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", idUsuario));
            HttpUtil.writeJson(resp, 200, Map.of("puntos", usuario.getPuntos()));
        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
