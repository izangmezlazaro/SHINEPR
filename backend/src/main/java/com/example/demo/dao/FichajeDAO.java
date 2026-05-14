package com.example.demo.dao;

import com.example.demo.entity.Fichaje;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FichajeDAO {

    public Fichaje save(Fichaje f) throws SQLException {
        String sql = "INSERT INTO fichaje (empleado_email, empleado_nombre, tipo, fecha_hora) " +
                     "VALUES (?, ?, ?, NOW()) RETURNING id, fecha_hora";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getEmpleadoEmail());
            ps.setString(2, f.getEmpleadoNombre());
            ps.setString(3, f.getTipo());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                f.setId(rs.getInt("id"));
                f.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
            }
        }
        return f;
    }

    public List<Fichaje> findByFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT id, empleado_email, empleado_nombre, tipo, fecha_hora " +
                     "FROM fichaje WHERE CAST(fecha_hora AS date) = ? ORDER BY fecha_hora ASC";
        List<Fichaje> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Fichaje> findAll() throws SQLException {
        String sql = "SELECT id, empleado_email, empleado_nombre, tipo, fecha_hora " +
                     "FROM fichaje ORDER BY fecha_hora DESC LIMIT 1000";
        List<Fichaje> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Fichaje mapRow(ResultSet rs) throws SQLException {
        Fichaje f = new Fichaje();
        f.setId(rs.getInt("id"));
        f.setEmpleadoEmail(rs.getString("empleado_email"));
        f.setEmpleadoNombre(rs.getString("empleado_nombre"));
        f.setTipo(rs.getString("tipo"));
        f.setFechaHora(rs.getTimestamp("fecha_hora").toLocalDateTime());
        return f;
    }
}
