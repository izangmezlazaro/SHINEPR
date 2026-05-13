package com.example.demo.service;

import com.example.demo.dao.FrascoDAO;
import com.example.demo.dto.FrascoDTO;
import com.example.demo.entity.Frasco;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class FrascoService {

    private final FrascoDAO frascoDAO;

    public FrascoService() {
        this.frascoDAO = new FrascoDAO();
    }

    public List<FrascoDTO> listar() {
        try {
            return frascoDAO.findAll().stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public FrascoDTO obtenerPorId(Integer id) {
        try {
            return toDto(findFrasco(id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public FrascoDTO crear(FrascoDTO request) {
        try {
            Frasco f = new Frasco();
            f.setForma(request.getForma());
            f.setCapacidadMl(request.getCapacidadMl());
            f.setMaterial(request.getMaterial());
            f.setPrecio(request.getPrecio());
            return toDto(frascoDAO.save(f));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Frasco findFrasco(Integer id) throws SQLException {
        return frascoDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Frasco", id));
    }

    private FrascoDTO toDto(Frasco f) {
        return new FrascoDTO(f.getIdFrasco(), f.getForma(), f.getCapacidadMl(), f.getMaterial(), f.getPrecio());
    }
}
