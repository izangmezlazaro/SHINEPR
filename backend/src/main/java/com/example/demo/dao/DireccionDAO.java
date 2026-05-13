package com.example.demo.dao;

import com.example.demo.entity.Direccion;
import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DireccionDAO {

    public List<Direccion> findByUsuarioId(Integer idUsuario) throws SQLException {
        String sql = "SELECT id, id_usuario, calle, ciudad, codigo_postal FROM direccion WHERE id_usuario = ?";
        List<Direccion> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Direccion> findById(Integer id) throws SQLException {
        String sql = "SELECT id, id_usuario, calle, ciudad, codigo_postal FROM direccion WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Direccion save(Direccion d) throws SQLException {
        if (d.getId() == null) {
            String sql = "INSERT INTO direccion (id_usuario, calle, ciudad, codigo_postal) " +
                         "VALUES (?,?,?,?) RETURNING id";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, d.getUsuario().getId());
                ps.setString(2, d.getCalle());
                ps.setString(3, d.getCiudad());
                ps.setString(4, d.getCodigoPostal());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    d.setId(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE direccion SET calle=?, ciudad=?, codigo_postal=? WHERE id=?";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, d.getCalle());
                ps.setString(2, d.getCiudad());
                ps.setString(3, d.getCodigoPostal());
                ps.setInt(4, d.getId());
                ps.executeUpdate();
            }
        }
        return d;
    }

    public void delete(Integer id) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM direccion WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Direccion mapRow(ResultSet rs) throws SQLException {
        Direccion d = new Direccion();
        d.setId(rs.getInt("id"));
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        d.setUsuario(u);
        d.setCalle(rs.getString("calle"));
        d.setCiudad(rs.getString("ciudad"));
        d.setCodigoPostal(rs.getString("codigo_postal"));
        return d;
    }
}
