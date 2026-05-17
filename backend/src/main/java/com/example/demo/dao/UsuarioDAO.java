package com.example.demo.dao;

import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.Optional;

public class UsuarioDAO {

    public Optional<Usuario> findById(Integer id) throws SQLException {
        String sql = "SELECT id, nombre, email, password, telefono, rol, puntos FROM usuario WHERE id = ?";
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
        String sql = "SELECT id, nombre, email, password, telefono, rol, puntos FROM usuario WHERE email = ?";
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
        String sql = "INSERT INTO usuario (nombre, email, password, telefono, rol, puntos) " +
                     "VALUES (?, ?, ?, ?, ?, 0) RETURNING id";
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
                usuario.setPuntos(0);
            }
        }
        return usuario;
    }

    private Usuario update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre=?, email=?, password=?, telefono=?, rol=?, puntos=? WHERE id=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getPassword());
            ps.setString(4, usuario.getTelefono());
            ps.setString(5, usuario.getRol());
            ps.setInt(6, usuario.getPuntos());
            ps.setInt(7, usuario.getId());
            ps.executeUpdate();
        }
        return usuario;
    }

    /** Devuelve todos los usuarios con rol 'empleado' o 'admin', ordenados por nombre. */
    public java.util.List<Usuario> findAllStaff() throws SQLException {
        String sql = "SELECT id, nombre, email, password, telefono, rol, puntos FROM usuario " +
                     "WHERE rol IN ('empleado','admin') ORDER BY nombre";
        java.util.List<Usuario> list = new java.util.ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /** Cambia el rol de un usuario. */
    public void updateRol(Integer id, String rol) throws SQLException {
        String sql = "UPDATE usuario SET rol = ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Elimina un usuario por ID. */
    public void deleteById(Integer id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Suma puntos al saldo actual del usuario (operación atómica en BD). */
    public void addPuntos(Integer idUsuario, int puntos) throws SQLException {
        String sql = "UPDATE usuario SET puntos = puntos + ? WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, puntos);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    private Usuario mapRow(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setTelefono(rs.getString("telefono"));
        u.setRol(rs.getString("rol"));
        u.setPuntos(rs.getInt("puntos"));
        return u;
    }
}
