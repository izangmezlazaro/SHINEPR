package com.example.demo.service;

import com.example.demo.dao.EnvioDAO;
import com.example.demo.dto.EnvioDTO;
import com.example.demo.dto.EstadoEnvioRequestDTO;
import com.example.demo.entity.Envio;
import com.example.demo.entity.Pedido;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;

public class EnvioService {

    private final EnvioDAO    envioDAO;
    private final PedidoService pedidoService;

    public EnvioService() {
        this.envioDAO     = new EnvioDAO();
        this.pedidoService = new PedidoService();
    }

    public EnvioDTO obtenerPorPedido(Integer idPedido) {
        try {
            return toDto(envioDAO.findByPedidoId(idPedido)
                    .orElseThrow(() -> new EntityNotFoundException("Envio del pedido " + idPedido)));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public EnvioDTO crear(EnvioDTO request) {
        try {
            if (envioDAO.findByPedidoId(request.getIdPedido()).isPresent()) {
                throw new BadRequestException("El pedido ya tiene un envio asociado");
            }
            Pedido pedido = pedidoService.findPedido(request.getIdPedido());
            Envio envio = new Envio();
            envio.setPedido(pedido);
            envio.setEstadoEnv(request.getEstadoEnv() == null || request.getEstadoEnv().isBlank()
                    ? "preparando" : request.getEstadoEnv());
            envio.setNumSeguimiento(request.getNumSeguimiento());
            return toDto(envioDAO.save(envio));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public EnvioDTO actualizarEstado(Integer idEnvio, EstadoEnvioRequestDTO request) {
        try {
            Envio envio = envioDAO.findById(idEnvio)
                    .orElseThrow(() -> new EntityNotFoundException("Envio", idEnvio));
            envio.setEstadoEnv(request.getEstadoEnv());
            if (request.getNumSeguimiento() != null && !request.getNumSeguimiento().isBlank()) {
                envio.setNumSeguimiento(request.getNumSeguimiento());
            }
            return toDto(envioDAO.save(envio));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private EnvioDTO toDto(Envio envio) {
        return new EnvioDTO(envio.getIdEnvio(), envio.getPedido().getIdPedido(),
                envio.getEstadoEnv(), envio.getNumSeguimiento());
    }
}
