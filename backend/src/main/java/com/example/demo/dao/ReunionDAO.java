package com.example.demo.dao;

import com.example.demo.entity.Reunion;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReunionDAO {

    public Reunion save(Reunion r) throws SQLException {
        String sql = "INSERT INTO reunion (titulo, fecha, hora, plataforma, asistentes, color, creado_por, creado_en) " +
                     "VALUES (?, ?::date, ?, ?, ?, ?, ?, NOW()) RETURNING id, creado_en";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getTitulo());
            ps.setString(2, r.getFecha());
            ps.setString(3, r.getHora());
            ps.setString(4, r.getPlataforma());
            ps.setString(5, r.getAsistentes());
            ps.setString(6, r.getColor() != null ? r.getColor() : "rose");
            ps.setString(7, r.getCreadoPor());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                r.setId(rs.getInt("id"));
                r.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
            }
        }
        return r;
    }

    public List<Reunion> findAll() throws SQLException {
        String sql = "SELECT id, titulo, fecha::text, hora, plataforma, asistentes, color, creado_por, creado_en " +
                     "FROM reunion ORDER BY fecha ASC, hora ASC";
        List<Reunion> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM reunion WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Reunion mapRow(ResultSet rs) throws SQLException {
        Reunion r = new Reunion();
        r.setId(rs.getInt("id"));
        r.setTitulo(rs.getString("titulo"));
        r.setFecha(rs.getString("fecha"));
        r.setHora(rs.getString("hora"));
        r.setPlataforma(rs.getString("plataforma"));
        r.setAsistentes(rs.getString("asistentes"));
        r.setColor(rs.getString("color"));
        r.setCreadoPor(rs.getString("creado_por"));
        Timestamp ts = rs.getTimestamp("creado_en");
        if (ts != null) r.setCreadoEn(ts.toLocalDateTime());
        return r;
    }
}
