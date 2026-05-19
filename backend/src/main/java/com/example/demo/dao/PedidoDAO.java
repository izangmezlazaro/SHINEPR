package com.example.demo.dao;

import com.example.demo.dto.AdminPedidoDTO;
import com.example.demo.entity.Direccion;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDAO {

    private static final String SELECT =
        "SELECT id_pedido, id_usuario, id_direccion, estado, total, fecha FROM pedido ";

    public Optional<Pedido> findById(Integer id) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_pedido = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public List<Pedido> findByUsuarioId(Integer idUsuario) throws SQLException {
        List<Pedido> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE id_usuario = ? ORDER BY id_pedido DESC")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Pedido save(Pedido p, Connection conn) throws SQLException {
        if (p.getIdPedido() == null) {
            return insert(p, conn);
        }
        return p;
    }

    private Pedido insert(Pedido p, Connection conn) throws SQLException {
        String sql = "INSERT INTO pedido (id_usuario, id_direccion, estado, total, fecha) " +
                     "VALUES (?,?,?,?,?) RETURNING id_pedido";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getUsuario().getId());
            ps.setInt(2, p.getDireccion().getId());
            ps.setString(3, p.getEstado() == null ? "pendiente" : p.getEstado());
            ps.setBigDecimal(4, p.getTotal());
            ps.setTimestamp(5, Timestamp.valueOf(
                p.getFecha() == null ? java.time.LocalDateTime.now() : p.getFecha()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.setIdPedido(rs.getInt(1));
            }
        }
        return p;
    }

    public List<AdminPedidoDTO> findAllAdmin() throws SQLException {
        String sql =
            "SELECT p.id_pedido, p.estado, p.total, p.fecha, " +
            "       u.nombre AS u_nombre, u.email AS u_email " +
            "FROM pedido p " +
            "JOIN usuario u ON p.id_usuario = u.id " +
            "ORDER BY p.id_pedido DESC";
        List<AdminPedidoDTO> result = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                AdminPedidoDTO dto = new AdminPedidoDTO();
                dto.setIdPedido(rs.getInt("id_pedido"));
                dto.setEstado(rs.getString("estado"));
                dto.setTotal(rs.getBigDecimal("total"));
                Timestamp ts = rs.getTimestamp("fecha");
                if (ts != null) dto.setFecha(ts.toLocalDateTime());
                dto.setNombreUsuario(rs.getString("u_nombre"));
                dto.setEmailUsuario(rs.getString("u_email"));
                result.add(dto);
            }
        }
        return result;
    }

    public void updateEstado(Integer idPedido, String estado) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE pedido SET estado = ? WHERE id_pedido = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    public void updateEstado(Integer idPedido, String estado, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE pedido SET estado = ? WHERE id_pedido = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    public String getEstadoActual(Integer idPedido, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT estado FROM pedido WHERE id_pedido = ?")) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("estado");
            }
        }
        return null;
    }

    private Pedido mapRow(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        Usuario u = new Usuario(); u.setId(rs.getInt("id_usuario")); p.setUsuario(u);
        Direccion d = new Direccion(); d.setId(rs.getInt("id_direccion")); p.setDireccion(d);
        p.setEstado(rs.getString("estado"));
        p.setTotal(rs.getBigDecimal("total"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) p.setFecha(ts.toLocalDateTime());
        return p;
    }
}
