package com.example.demo;
import com.example.demo.service.PedidoService;
import com.example.demo.dto.PedidoResponseDTO;
import com.example.demo.service.FacturaService;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("Test PedidoService...");
        try {
            PedidoService ps = new PedidoService();
            PedidoResponseDTO dto = ps.obtenerPorId(31);
            System.out.println("Pedido DTO: " + dto.getIdPedido() + ", User ID: " + dto.getIdUsuario());
            
            System.out.println("Probando FacturaService...");
            FacturaService fs = new FacturaService();
            byte[] pdf = fs.generarFacturaPdf(dto);
            System.out.println("Factura generada: " + pdf.length + " bytes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
