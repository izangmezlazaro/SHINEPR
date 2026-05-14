package com.example.demo.service;

import com.example.demo.dao.SubcategoriaDAO;
import com.example.demo.dto.SubcategoriaDTO;
import com.example.demo.entity.Subcategoria;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SubcategoriaService {

    private final SubcategoriaDAO subcategoriaDAO;

    public SubcategoriaService() {
        this.subcategoriaDAO = new SubcategoriaDAO();
    }

    public List<SubcategoriaDTO> listar() {
        try {
            return subcategoriaDAO.findAll().stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public List<SubcategoriaDTO> listarPorCategoria(Integer idCategoria) {
        try {
            return subcategoriaDAO.findByCategoria(idCategoria).stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private SubcategoriaDTO toDto(Subcategoria s) {
        return new SubcategoriaDTO(s.getIdSubcategoria(), s.getNombre(), s.getIdCategoria());
    }
}
