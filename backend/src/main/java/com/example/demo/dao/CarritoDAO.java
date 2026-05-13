package com.example.demo.dao;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class CarritoDAO {

    public Optional<Carrito> findByUsuarioId(Integer idUsuario) throws SQLException {
        String sql = "SELECT id, id_usuario, creado_en FROM carrito WHERE id_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Carrito> findById(Integer id) throws SQLException {
        String sql = "SELECT id, id_usuario, creado_en FROM carrito WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Carrito save(Carrito carrito) throws SQLException {
        if (carrito.getId() == null) {
            String sql = "INSERT INTO carrito (id_usuario, creado_en) VALUES (?, ?) RETURNING id";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, carrito.getUsuario().getId());
                ps.setTimestamp(2, Timestamp.valueOf(
                    carrito.getCreadoEn() == null ? LocalDateTime.now() : carrito.getCreadoEn()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    carrito.setId(rs.getInt(1));
                }
            }
        }
        return carrito;
    }

    private Carrito mapRow(ResultSet rs) throws SQLException {
        Carrito c = new Carrito();
        c.setId(rs.getInt("id"));
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        c.setUsuario(u);
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) c.setCreadoEn(ts.toLocalDateTime());
        return c;
    }
}
