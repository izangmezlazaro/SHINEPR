package com.example.demo.dao;

import com.example.demo.entity.Anuncio;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnuncioDAO {

    public List<Anuncio> findAll() throws SQLException {
        List<Anuncio> lista = new ArrayList<>();
        String sql = "SELECT id, titulo, tag, tag_label, mensaje, autor, fecha FROM anuncio ORDER BY fecha DESC";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        }
        return lista;
    }

    public Anuncio save(Anuncio a) throws SQLException {
        String sql = "INSERT INTO anuncio (titulo, tag, tag_label, mensaje, autor, fecha) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id, fecha";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitulo());
            ps.setString(2, a.getTag());
            ps.setString(3, a.getTagLabel());
            ps.setString(4, a.getMensaje());
            ps.setString(5, a.getAutor());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    a.setId(rs.getInt(1));
                    a.setFecha(rs.getTimestamp(2).toLocalDateTime());
                }
            }
        }
        return a;
    }

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM anuncio WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Anuncio mapRow(ResultSet rs) throws SQLException {
        Anuncio a = new Anuncio();
        a.setId(rs.getInt("id"));
        a.setTitulo(rs.getString("titulo"));
        a.setTag(rs.getString("tag"));
        a.setTagLabel(rs.getString("tag_label"));
        a.setMensaje(rs.getString("mensaje"));
        a.setAutor(rs.getString("autor"));
        a.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        return a;
    }
}