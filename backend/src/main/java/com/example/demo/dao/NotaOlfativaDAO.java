package com.example.demo.dao;

import com.example.demo.entity.Fragancia;
import com.example.demo.entity.NotaOlfativa;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotaOlfativaDAO {

    public List<NotaOlfativa> findAll() throws SQLException {
        String sql = "SELECT id_nota, id_fragancia, nombre, tipo, url_imagen FROM nota_olfativa ORDER BY id_nota";
        List<NotaOlfativa> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<NotaOlfativa> findByFraganciaId(Integer idFragancia) throws SQLException {
        String sql = "SELECT id_nota, id_fragancia, nombre, tipo, url_imagen FROM nota_olfativa WHERE id_fragancia = ?";
        List<NotaOlfativa> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFragancia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public NotaOlfativa save(NotaOlfativa n) throws SQLException {
        if (n.getIdNota() == null) {
            String sql = "INSERT INTO nota_olfativa (id_fragancia, nombre, tipo, url_imagen) " +
                         "VALUES (?,?,?,?) RETURNING id_nota";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, n.getFragancia().getIdFragancia());
                ps.setString(2, n.getNombre());
                ps.setString(3, n.getTipo());
                ps.setString(4, n.getUrlImagen());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    n.setIdNota(rs.getInt(1));
                }
            }
        }
        return n;
    }

    private NotaOlfativa mapRow(ResultSet rs) throws SQLException {
        NotaOlfativa n = new NotaOlfativa();
        n.setIdNota(rs.getInt("id_nota"));
        Fragancia f = new Fragancia(); f.setIdFragancia(rs.getInt("id_fragancia")); n.setFragancia(f);
        n.setNombre(rs.getString("nombre"));
        n.setTipo(rs.getString("tipo"));
        n.setUrlImagen(rs.getString("url_imagen"));
        return n;
    }
}
