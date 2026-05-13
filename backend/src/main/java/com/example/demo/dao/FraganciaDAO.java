package com.example.demo.dao;

import com.example.demo.entity.Fragancia;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FraganciaDAO {

    public List<Fragancia> findAll() throws SQLException {
        String sql = "SELECT id_fragancia, nombre, familia, es_base FROM fragancia ORDER BY id_fragancia";
        List<Fragancia> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Fragancia> findById(Integer id) throws SQLException {
        String sql = "SELECT id_fragancia, nombre, familia, es_base FROM fragancia WHERE id_fragancia = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Fragancia save(Fragancia f) throws SQLException {
        if (f.getIdFragancia() == null) {
            String sql = "INSERT INTO fragancia (nombre, familia, es_base) VALUES (?,?,?) RETURNING id_fragancia";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, f.getNombre());
                ps.setString(2, f.getFamilia());
                ps.setBoolean(3, Boolean.TRUE.equals(f.getEsBase()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    f.setIdFragancia(rs.getInt(1));
                }
            }
        }
        return f;
    }

    public Fragancia mapRow(ResultSet rs) throws SQLException {
        return new Fragancia(
            rs.getInt("id_fragancia"),
            rs.getString("nombre"),
            rs.getString("familia"),
            rs.getBoolean("es_base")
        );
    }
}
