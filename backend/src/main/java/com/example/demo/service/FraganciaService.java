package com.example.demo.service;

import com.example.demo.dao.FraganciaDAO;
import com.example.demo.dto.FraganciaDTO;
import com.example.demo.entity.Fragancia;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FraganciaService {

    private final FraganciaDAO fraganciaDAO;

    public FraganciaService() {
        this.fraganciaDAO = new FraganciaDAO();
    }

    public List<FraganciaDTO> listar() {
        try {
            return fraganciaDAO.findAll().stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public FraganciaDTO obtenerPorId(Integer id) {
        try {
            return toDto(fraganciaDAO.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Fragancia", id)));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public FraganciaDTO crear(FraganciaDTO request) {
        try {
            Fragancia f = new Fragancia();
            f.setNombre(request.getNombre());
            f.setFamilia(request.getFamilia());
            f.setEsBase(Boolean.TRUE.equals(request.getEsBase()));
            return toDto(fraganciaDAO.save(f));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Fragancia findFragancia(Integer id) {
        try {
            return fraganciaDAO.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Fragancia", id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private FraganciaDTO toDto(Fragancia f) {
        return new FraganciaDTO(f.getIdFragancia(), f.getNombre(), f.getFamilia(), f.getEsBase());
    }
}
