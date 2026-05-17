package com.example.demo.servlet;

import com.example.demo.dto.AuthLoginRequestDTO;
import com.example.demo.dto.AuthRegisterRequestDTO;
import com.example.demo.exception.BadRequestException;
import com.example.demo.service.AuthService;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/v1/auth/register  → registrar nuevo usuario
 * POST /api/v1/auth/login     → iniciar sesión
 */
@WebServlet(urlPatterns = "/api/v1/auth/*", name = "AuthServlet")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo(); // "/register" o "/login"
        try {
            if ("/register".equals(path)) {
                AuthRegisterRequestDTO dto = com.example.demo.util.JsonUtil.fromJson(
                        HttpUtil.readBody(req), AuthRegisterRequestDTO.class);
                HttpUtil.writeJson(resp, 201, authService.register(dto));

            } else if ("/register-staff".equals(path)) {
                AuthRegisterRequestDTO dto = com.example.demo.util.JsonUtil.fromJson(
                        HttpUtil.readBody(req), AuthRegisterRequestDTO.class);
                HttpUtil.writeJson(resp, 201, authService.registerStaff(dto));

            } else if ("/login".equals(path)) {
                AuthLoginRequestDTO dto = com.example.demo.util.JsonUtil.fromJson(
                        HttpUtil.readBody(req), AuthLoginRequestDTO.class);
                HttpUtil.writeJson(resp, 200, authService.login(dto));

            } else {
                HttpUtil.writeError(resp, 404, "Ruta no encontrada");
            }
        } catch (BadRequestException e) {
            HttpUtil.writeError(resp, 400, e.getMessage());
        } catch (com.example.demo.exception.EntityNotFoundException e) {
            HttpUtil.writeError(resp, 404, e.getMessage());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
