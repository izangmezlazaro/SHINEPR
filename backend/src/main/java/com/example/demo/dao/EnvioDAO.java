package com.example.demo.dao;

import com.example.demo.entity.Envio;
import com.example.demo.entity.Pedido;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.Optional;

public class EnvioDAO {

    public Optional<Envio> findByPedidoId(Integer idPedido) throws SQLException {
        String sql = "SELECT id_envio, id_pedido, estado_env, num_seguimiento FROM envio WHERE id_pedido = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Envio> findById(Integer id) throws SQLException {
        String sql = "SELECT id_envio, id_pedido, estado_env, num_seguimiento FROM envio WHERE id_envio = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Envio save(Envio envio) throws SQLException {
        if (envio.getIdEnvio() == null) {
            String sql = "INSERT INTO envio (id_pedido, estado_env, num_seguimiento) VALUES (?,?,?) RETURNING id_envio";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, envio.getPedido().getIdPedido());
                ps.setString(2, envio.getEstadoEnv());
                ps.setString(3, envio.getNumSeguimiento());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    envio.setIdEnvio(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE envio SET estado_env=?, num_seguimiento=? WHERE id_envio=?";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, envio.getEstadoEnv());
                ps.setString(2, envio.getNumSeguimiento());
                ps.setInt(3, envio.getIdEnvio());
                ps.executeUpdate();
            }
        }
        return envio;
    }

    private Envio mapRow(ResultSet rs) throws SQLException {
        Envio e = new Envio();
        e.setIdEnvio(rs.getInt("id_envio"));
        Pedido p = new Pedido(); p.setIdPedido(rs.getInt("id_pedido")); e.setPedido(p);
        e.setEstadoEnv(rs.getString("estado_env"));
        e.setNumSeguimiento(rs.getString("num_seguimiento"));
        return e;
    }
}
