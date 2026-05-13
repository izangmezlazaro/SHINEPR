package com.example.demo.dao;

import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    public Optional<Usuario> findById(Integer id) throws SQLException {
        String sql = "SELECT id, nombre, email, password, telefono, rol FROM usuario WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, nombre, email, password, telefono, rol FROM usuario WHERE email = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Inserta y devuelve el usuario con su ID generado. */
    public Usuario save(Usuario usuario) throws SQLException {
        if (usuario.getId() == null) {
            return insert(usuario);
        } else {
            return update(usuario);
        }
    }

    private Usuario insert(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, password, telefono, rol) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getRol() == null ? "cliente" : usuario.getRol());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                usuario.setId(rs.getInt(1));
            }
        }
        return usuario;
    }

    private Usuario update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre=?, email=?, password=?, telefono=?, rol=? WHERE id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getRol());
            ps.setInt(6, usuario.getId());
            ps.executeUpdate();
        }
        return usuario;
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setTelefono(rs.getString("telefono"));
        u.setRol(rs.getString("rol"));
        return u;
    }
}
