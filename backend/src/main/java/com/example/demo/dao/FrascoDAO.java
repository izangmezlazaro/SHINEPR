package com.example.demo.dao;

import com.example.demo.entity.Frasco;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FrascoDAO {

    public List<Frasco> findAll() throws SQLException {
        String sql = "SELECT id_frasco, forma, capacidad_ml, material, precio FROM frasco ORDER BY id_frasco";
        List<Frasco> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Frasco> findById(Integer id) throws SQLException {
        String sql = "SELECT id_frasco, forma, capacidad_ml, material, precio FROM frasco WHERE id_frasco = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Frasco save(Frasco f) throws SQLException {
        if (f.getIdFrasco() == null) {
            String sql = "INSERT INTO frasco (forma, capacidad_ml, material, precio) VALUES (?,?,?,?) RETURNING id_frasco";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, f.getForma());
                ps.setInt(2, f.getCapacidadMl());
                ps.setString(3, f.getMaterial());
                ps.setBigDecimal(4, f.getPrecio());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    f.setIdFrasco(rs.getInt(1));
                }
            }
        }
        return f;
    }

    public Frasco mapRow(ResultSet rs) throws SQLException {
        return new Frasco(
            rs.getInt("id_frasco"),
            rs.getString("forma"),
            rs.getInt("capacidad_ml"),
            rs.getString("material"),
            rs.getBigDecimal("precio")
        );
    }
}
