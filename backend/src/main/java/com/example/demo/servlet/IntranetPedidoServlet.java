package com.example.demo.servlet;

import com.example.demo.service.PedidoService;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/v1/intranet/pedidos → lista todos los pedidos con datos del cliente (uso staff)
 */
@WebServlet(urlPatterns = "/api/v1/intranet/pedidos/*", name = "IntranetPedidoServlet")
public class IntranetPedidoServlet extends HttpServlet {

    private final PedidoService pedidoService = new PedidoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, pedidoService.listarTodosAdmin());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
