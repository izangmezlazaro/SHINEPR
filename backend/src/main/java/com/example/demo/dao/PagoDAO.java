package com.example.demo.dao;

import com.example.demo.entity.Pago;
import com.example.demo.entity.Pedido;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class PagoDAO {

    public Optional<Pago> findByPedidoId(Integer idPedido) throws SQLException {
        String sql = "SELECT id_pago, id_pedido, metodo_pago, fecha_pago, estado FROM pago WHERE id_pedido = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Pago> findById(Integer id) throws SQLException {
        String sql = "SELECT id_pago, id_pedido, metodo_pago, fecha_pago, estado FROM pago WHERE id_pago = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Pago save(Pago pago) throws SQLException {
        if (pago.getIdPago() == null) {
            String sql = "INSERT INTO pago (id_pedido, metodo_pago, fecha_pago, estado) VALUES (?,?,?,?) RETURNING id_pago";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, pago.getPedido().getIdPedido());
                ps.setString(2, pago.getMetodoPago());
                ps.setTimestamp(3, Timestamp.valueOf(
                    pago.getFechaPago() == null ? LocalDateTime.now() : pago.getFechaPago()));
                ps.setString(4, pago.getEstado() == null ? "pendiente" : pago.getEstado());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    pago.setIdPago(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE pago SET estado=? WHERE id_pago=?";
            try (Connection conn = ConexionDB.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, pago.getEstado());
                ps.setInt(2, pago.getIdPago());
                ps.executeUpdate();
            }
        }
        return pago;
    }

    private Pago mapRow(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setIdPago(rs.getInt("id_pago"));
        Pedido ped = new Pedido(); ped.setIdPedido(rs.getInt("id_pedido")); p.setPedido(ped);
        p.setMetodoPago(rs.getString("metodo_pago"));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) p.setFechaPago(ts.toLocalDateTime());
        p.setEstado(rs.getString("estado"));
        return p;
    }
}
