package com.example.demo.dao;

import com.example.demo.entity.DetallePedido;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.PerfumeCustom;
import com.example.demo.entity.Producto;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAO {

    public List<DetallePedido> findByPedidoId(Integer idPedido) throws SQLException {
        String sql = "SELECT id, id_pedido, id_producto, id_perf_cust, cantidad, precio_unitario " +
                     "FROM detalle_pedido WHERE id_pedido = ?";
        List<DetallePedido> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void save(DetallePedido detalle, Connection conn) throws SQLException {
        String sql = "INSERT INTO detalle_pedido (id_pedido, id_producto, id_perf_cust, cantidad, precio_unitario) " +
                     "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detalle.getPedido().getIdPedido());
            if (detalle.getProducto() != null) ps.setInt(2, detalle.getProducto().getIdProducto());
            else ps.setNull(2, Types.INTEGER);
            if (detalle.getPerfumeCustom() != null) ps.setInt(3, detalle.getPerfumeCustom().getIdPerfCust());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, detalle.getCantidad());
            ps.setBigDecimal(5, detalle.getPrecioUnitario());
            ps.executeUpdate();
        }
    }

    private DetallePedido mapRow(ResultSet rs) throws SQLException {
        DetallePedido d = new DetallePedido();
        d.setId(rs.getInt("id"));
        Pedido p = new Pedido(); p.setIdPedido(rs.getInt("id_pedido")); d.setPedido(p);
        int idProd = rs.getInt("id_producto");
        if (!rs.wasNull()) { Producto prod = new Producto(); prod.setIdProducto(idProd); d.setProducto(prod); }
        int idPC = rs.getInt("id_perf_cust");
        if (!rs.wasNull()) { PerfumeCustom pc = new PerfumeCustom(); pc.setIdPerfCust(idPC); d.setPerfumeCustom(pc); }
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        return d;
    }
}
