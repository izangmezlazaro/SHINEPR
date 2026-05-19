package com.example.demo.servlet;

import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.service.FacturaService;
import com.example.demo.service.PedidoService;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/v1/facturas/{id}
 * Descarga la factura en PDF de un pedido específico.
 */
@WebServlet(urlPatterns = "/api/v1/facturas/*", name = "FacturaServlet")
public class FacturaServlet extends HttpServlet {

    private final PedidoService pedidoService = new PedidoService();
    private final FacturaService facturaService = new FacturaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null) {
                HttpUtil.writeError(resp, 400, "Ruta inválida");
                return;
            }
            
            // Expected path: /42 (where 42 is the order ID)
            String[] parts = path.split("/");
            if (parts.length < 2) {
                HttpUtil.writeError(resp, 400, "Falta el ID del pedido");
                return;
            }
            
            int idPedido = Integer.parseInt(parts[1]);
            
            // Obtener el pedido (lanzará EntityNotFoundException si no existe o si está protegido por JWT y no pertenece al usuario, pero eso se maneja en el servicio/DAO)
            PedidoResponseDTO pedido = pedidoService.obtenerPorId(idPedido);
            
            // Generar PDF
            byte[] pdfBytes = facturaService.generarFacturaPdf(pedido);
            
            // Configurar cabeceras de respuesta para forzar descarga
            resp.setContentType("application/pdf");
            resp.setHeader("Content-Disposition", "attachment; filename=\"factura_SHINE-" + idPedido + ".pdf\"");
            resp.setContentLength(pdfBytes.length);
            
            // Escribir bytes
            resp.getOutputStream().write(pdfBytes);
            resp.getOutputStream().flush();
            
        } catch (NumberFormatException e) {
            HttpUtil.writeError(resp, 400, "ID de pedido no válido");
        } catch (Exception e) {
            System.err.println("[FacturaServlet] ERROR: " + e.getMessage());
            e.printStackTrace(System.err);
            HttpUtil.writeError(resp, 500, "Error generando factura: " + e.getMessage());
        }
    }
}
