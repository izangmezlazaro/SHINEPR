package com.example.demo.service;

import com.example.demo.dao.DireccionDAO;
import com.example.demo.dao.UsuarioDAO;
import com.example.demo.dto.DireccionDTO;
import com.example.demo.entity.Direccion;
import com.example.demo.entity.Usuario;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DireccionService {

    private final DireccionDAO direccionDAO;
    private final UsuarioDAO   usuarioDAO;

    public DireccionService() {
        this.direccionDAO = new DireccionDAO();
        this.usuarioDAO   = new UsuarioDAO();
    }

    public List<DireccionDTO> listarPorUsuario(Integer idUsuario) {
        try {
            return direccionDAO.findByUsuarioId(idUsuario).stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public DireccionDTO crear(DireccionDTO request) {
        try {
            Usuario usuario = usuarioDAO.findById(request.getIdUsuario())
                    .orElseThrow(() -> new EntityNotFoundException("Usuario", request.getIdUsuario()));
            Direccion d = new Direccion();
            d.setUsuario(usuario);
            d.setCalle(request.getCalle());
            d.setCiudad(request.getCiudad());
            d.setCodigoPostal(request.getCodigoPostal());
            return toDto(direccionDAO.save(d));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public DireccionDTO actualizar(Integer idDireccion, DireccionDTO request) {
        try {
            Direccion d = direccionDAO.findById(idDireccion)
                    .orElseThrow(() -> new EntityNotFoundException("Direccion", idDireccion));
            if (!d.getUsuario().getId().equals(request.getIdUsuario())) {
                throw new BadRequestException("La direccion no pertenece al usuario indicado");
            }
            d.setCalle(request.getCalle());
            d.setCiudad(request.getCiudad());
            d.setCodigoPostal(request.getCodigoPostal());
            return toDto(direccionDAO.save(d));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void eliminar(Integer idDireccion) {
        try {
            direccionDAO.findById(idDireccion)
                    .orElseThrow(() -> new EntityNotFoundException("Direccion", idDireccion));
            direccionDAO.delete(idDireccion);
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private DireccionDTO toDto(Direccion d) {
        return new DireccionDTO(d.getId(), d.getUsuario().getId(), d.getCalle(), d.getCiudad(), d.getCodigoPostal());
    }
}
