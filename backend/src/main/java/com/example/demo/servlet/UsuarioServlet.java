package com.example.demo.servlet;

import com.example.demo.dao.UsuarioDAO;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.util.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GET    /api/v1/usuarios          → lista todo el staff (empleado + admin)
 * GET    /api/v1/usuarios/{id}/puntos → puntos de fidelidad del usuario
 * PUT    /api/v1/usuarios/{id}/rol → cambiar el rol de un usuario
 * DELETE /api/v1/usuarios/{id}     → eliminar usuario
 */
@WebServlet(urlPatterns = "/api/v1/usuarios/*", name = "UsuarioServlet")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo(); // null → lista staff; "/{id}/puntos"

            if (path == null || path.equals("/")) {
                // GET /api/v1/usuarios → lista staff
                List<Map<String, Object>> result = usuarioDAO.findAllStaff().stream()
                        .map(this::toMap)
                        .collect(Collectors.toList());
                HttpUtil.writeJson(resp, 200, result);
                return;
            }

            if (path.endsWith("/puntos")) {
                String withoutPuntos = path.substring(0, path.lastIndexOf("/puntos"));
                int idUsuario = HttpUtil.extractId(withoutPuntos);
                Usuario usuario = usuarioDAO.findById(idUsuario)
                        .orElseThrow(() -> new EntityNotFoundException("Usuario", idUsuario));
                HttpUtil.writeJson(resp, 200, Map.of("puntos", usuario.getPuntos()));
                return;
            }

            HttpUtil.writeError(resp, 404, "Ruta no encontrada");

        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo(); // "/{id}/rol"
            if (path == null || !path.endsWith("/rol")) {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada");
                return;
            }

            String withoutRol = path.substring(0, path.lastIndexOf("/rol"));
            int id = HttpUtil.extractId(withoutRol);

            String body = HttpUtil.readBody(req);
            JsonObject json = gson.fromJson(body, JsonObject.class);
            String newRol = json.has("rol") ? json.get("rol").getAsString() : null;

            if (newRol == null || (!newRol.equals("empleado") && !newRol.equals("admin") && !newRol.equals("cliente"))) {
                HttpUtil.writeError(resp, 400, "Rol no válido. Usa 'empleado', 'admin' o 'cliente'.");
                return;
            }

            usuarioDAO.updateRol(id, newRol);
            HttpUtil.writeJson(resp, 200, Map.of("mensaje", "Rol actualizado correctamente"));

        } catch (EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo(); // "/{id}"
            if (path == null || path.equals("/")) {
                HttpUtil.writeError(resp, 400, "ID requerido");
                return;
            }
            int id = HttpUtil.extractId(path);
            usuarioDAO.deleteById(id);
            HttpUtil.writeJson(resp, 200, Map.of("mensaje", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }

    private Map<String, Object> toMap(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("nombre", u.getNombre());
        m.put("email", u.getEmail());
        m.put("rol", u.getRol());
        m.put("telefono", u.getTelefono());
        m.put("puntos", u.getPuntos());
        return m;
    }
}
