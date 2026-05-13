package com.example.demo.dao;

import com.example.demo.entity.Categoria;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    public List<Categoria> findAll() throws SQLException {
        String sql = "SELECT id_categoria, nombre FROM categoria ORDER BY id_categoria";
        List<Categoria> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Categoria> findById(Integer id) throws SQLException {
        String sql = "SELECT id_categoria, nombre FROM categoria WHERE id_categoria = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Categoria save(Categoria categoria) throws SQLException {
        if (categoria.getIdCategoria() == null) {
            String sql = "INSERT INTO categoria (nombre) VALUES (?) RETURNING id_categoria";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoria.getNombre());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    categoria.setIdCategoria(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE categoria SET nombre=? WHERE id_categoria=?";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoria.getNombre());
                ps.setInt(2, categoria.getIdCategoria());
                ps.executeUpdate();
            }
        }
        return categoria;
    }

    public Categoria mapRow(ResultSet rs) throws SQLException {
        return new Categoria(rs.getInt("id_categoria"), rs.getString("nombre"));
    }
}
