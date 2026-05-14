package com.example.demo.dao;

import com.example.demo.entity.Subcategoria;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubcategoriaDAO {

    public List<Subcategoria> findAll() throws SQLException {
        List<Subcategoria> list = new ArrayList<>();
        String sql = "SELECT id_subcategoria, nombre, id_categoria FROM subcategoria ORDER BY id_categoria, id_subcategoria";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Subcategoria> findByCategoria(Integer idCategoria) throws SQLException {
        List<Subcategoria> list = new ArrayList<>();
        String sql = "SELECT id_subcategoria, nombre, id_categoria FROM subcategoria WHERE id_categoria = ? ORDER BY id_subcategoria";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Subcategoria mapRow(ResultSet rs) throws SQLException {
        return new Subcategoria(
            rs.getInt("id_subcategoria"),
            rs.getString("nombre"),
            rs.getInt("id_categoria")
        );
    }
}
