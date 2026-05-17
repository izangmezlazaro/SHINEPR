package com.example.demo.dao;

import com.example.demo.entity.Fichaje;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FichajeDAO {

    /** Registra la hora de entrada (INSERT). */
    public Fichaje registrarEntrada(String email, String nombre) throws SQLException {
        String sql = "INSERT INTO fichaje (empleado_email, empleado_nombre, fecha, hora_entrada, estado) " +
                     "VALUES (?, ?, CURRENT_DATE, NOW(), 'FICHADO') RETURNING id, fecha, hora_entrada, estado";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                Fichaje f = new Fichaje();
                f.setId(rs.getInt("id"));
                f.setEmpleadoEmail(email);
                f.setEmpleadoNombre(nombre);
                f.setFecha(rs.getDate("fecha").toLocalDate());
                f.setHoraEntrada(rs.getTimestamp("hora_entrada").toLocalDateTime());
                f.setEstado(rs.getString("estado"));
                return f;
            }
        }
    }

    /** Registra la hora de salida del fichaje abierto de hoy. */
    public int registrarSalida(String email) throws SQLException {
        String sql = "UPDATE fichaje SET hora_salida = NOW(), estado = 'COMPLETADO' " +
                     "WHERE empleado_email = ? AND fecha = CURRENT_DATE AND hora_salida IS NULL";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeUpdate();
        }
    }

    /** Lista fichajes de una fecha concreta. */
    public List<Fichaje> findByFecha(LocalDate fecha) throws SQLException {
        String sql = "SELECT id, empleado_email, empleado_nombre, fecha, hora_entrada, hora_salida, estado " +
                     "FROM fichaje WHERE fecha = ? ORDER BY hora_entrada ASC NULLS LAST";
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

    /** Lista todos los fichajes. */
    public List<Fichaje> findAll() throws SQLException {
        String sql = "SELECT id, empleado_email, empleado_nombre, fecha, hora_entrada, hora_salida, estado " +
                     "FROM fichaje ORDER BY fecha DESC, hora_entrada DESC LIMIT 500";
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
        f.setFecha(rs.getDate("fecha").toLocalDate());
        Timestamp entrada = rs.getTimestamp("hora_entrada");
        if (entrada != null) f.setHoraEntrada(entrada.toLocalDateTime());
        Timestamp salida = rs.getTimestamp("hora_salida");
        if (salida != null) f.setHoraSalida(salida.toLocalDateTime());
        f.setEstado(rs.getString("estado"));
        return f;
    }
}
