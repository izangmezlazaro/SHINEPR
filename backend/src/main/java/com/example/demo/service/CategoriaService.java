package com.example.demo.service;

import com.example.demo.dao.CategoriaDAO;
import com.example.demo.dto.CategoriaDTO;
import com.example.demo.entity.Categoria;
import com.example.demo.exception.EntityNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService() {
        this.categoriaDAO = new CategoriaDAO();
    }

    public List<CategoriaDTO> listar() {
        try {
            return categoriaDAO.findAll().stream().map(this::toDto).collect(Collectors.toList());
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public CategoriaDTO obtenerPorId(Integer id) {
        try {
            return toDto(categoriaDAO.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria", id)));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public CategoriaDTO crear(CategoriaDTO request) {
        try {
            Categoria cat = new Categoria();
            cat.setNombre(request.getNombre());
            return toDto(categoriaDAO.save(cat));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    Categoria findCategoria(Integer id) {
        try {
            return categoriaDAO.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria", id));
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private CategoriaDTO toDto(Categoria c) {
        return new CategoriaDTO(c.getIdCategoria(), c.getNombre());
    }
}
