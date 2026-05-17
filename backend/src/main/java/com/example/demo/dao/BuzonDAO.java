package com.example.demo.dao;

import com.example.demo.entity.BuzonMensaje;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuzonDAO {

    public BuzonMensaje save(BuzonMensaje m) throws SQLException {
        String sql = "INSERT INTO buzon_mensajes (nombre, apellidos, asunto, texto, fecha) " +
                     "VALUES (?, ?, ?, ?, NOW()) RETURNING id_mensaje, fecha";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getNombre());
            ps.setString(2, m.getApellidos());
            ps.setString(3, m.getAsunto());
            ps.setString(4, m.getTexto());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.setIdMensaje(rs.getInt("id_mensaje"));
                m.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
            }
        }
        return m;
    }

    public List<BuzonMensaje> findAll() throws SQLException {
        String sql = "SELECT id_mensaje, nombre, apellidos, asunto, texto, fecha " +
                     "FROM buzon_mensajes ORDER BY fecha DESC";
        List<BuzonMensaje> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private BuzonMensaje mapRow(ResultSet rs) throws SQLException {
        BuzonMensaje m = new BuzonMensaje();
        m.setIdMensaje(rs.getInt("id_mensaje"));
        m.setNombre(rs.getString("nombre"));
        m.setApellidos(rs.getString("apellidos"));
        m.setAsunto(rs.getString("asunto"));
        m.setTexto(rs.getString("texto"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) m.setFecha(ts.toLocalDateTime());
        return m;
    }
}
