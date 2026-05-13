package com.example.demo.service;

import com.example.demo.dao.PagoDAO;
import com.example.demo.dto.PagoDTO;
import com.example.demo.entity.Pago;
import com.example.demo.entity.Pedido;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;

public class PagoService {

    private static final String METODO_PAGO_PERMITIDO = "bizum";

    private final PagoDAO     pagoDAO;
    private final PedidoService pedidoService;

    public PagoService() {
        this.pagoDAO       = new PagoDAO();
        this.pedidoService = new PedidoService();
    }

    public PagoDTO obtenerPorPedido(Integer idPedido) {
        try {
            return toDto(pagoDAO.findByPedidoId(idPedido)
                    .orElseThrow(() -> new EntityNotFoundException("Pago de pedido " + idPedido)));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PagoDTO crear(PagoDTO request) {
        try {
            if (request.getMetodoPago() == null || !METODO_PAGO_PERMITIDO.equals(request.getMetodoPago())) {
                throw new BadRequestException("El unico metodo de pago permitido es bizum");
            }
            if (pagoDAO.findByPedidoId(request.getIdPedido()).isPresent()) {
                throw new BadRequestException("El pedido ya tiene un pago asociado");
            }
            Pedido pedido = pedidoService.findPedido(request.getIdPedido());
            Pago pago = new Pago();
            pago.setPedido(pedido);
            pago.setMetodoPago(request.getMetodoPago());
            pago.setEstado(request.getEstado() == null || request.getEstado().isBlank() ? "pendiente" : request.getEstado());
            pago.setFechaPago(request.getFechaPago());
            return toDto(pagoDAO.save(pago));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public PagoDTO actualizarEstado(Integer idPago, String estado) {
        try {
            Pago pago = pagoDAO.findById(idPago)
                    .orElseThrow(() -> new EntityNotFoundException("Pago", idPago));
            pago.setEstado(estado);
            return toDto(pagoDAO.save(pago));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private PagoDTO toDto(Pago pago) {
        return new PagoDTO(pago.getIdPago(), pago.getPedido().getIdPedido(),
                pago.getMetodoPago(), pago.getFechaPago(), pago.getEstado());
    }
}
