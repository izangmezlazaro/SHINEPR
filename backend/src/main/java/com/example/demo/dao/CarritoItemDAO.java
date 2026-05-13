package com.example.demo.dao;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.CarritoItem;
import com.example.demo.entity.PerfumeCustom;
import com.example.demo.entity.Producto;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarritoItemDAO {

    public List<CarritoItem> findByCarritoId(Integer idCarrito) throws SQLException {
        String sql = "SELECT id, id_carrito, id_producto, id_perf_cust, cantidad " +
                     "FROM carrito_item WHERE id_carrito = ?";
        List<CarritoItem> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<CarritoItem> findById(Integer id) throws SQLException {
        String sql = "SELECT id, id_carrito, id_producto, id_perf_cust, cantidad FROM carrito_item WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public CarritoItem save(CarritoItem item) throws SQLException {
        String sql = "INSERT INTO carrito_item (id_carrito, id_producto, id_perf_cust, cantidad) " +
                     "VALUES (?,?,?,?) RETURNING id";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getCarrito().getId());
            if (item.getProducto() != null) ps.setInt(2, item.getProducto().getIdProducto());
            else ps.setNull(2, Types.INTEGER);
            if (item.getPerfumeCustom() != null) ps.setInt(3, item.getPerfumeCustom().getIdPerfCust());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, item.getCantidad() == null ? 1 : item.getCantidad());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                item.setId(rs.getInt(1));
            }
        }
        return item;
    }

    public void delete(Integer id) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM carrito_item WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByCarritoId(Integer idCarrito) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM carrito_item WHERE id_carrito = ?")) {
            ps.setInt(1, idCarrito);
            ps.executeUpdate();
        }
    }

    private CarritoItem mapRow(ResultSet rs) throws SQLException {
        CarritoItem item = new CarritoItem();
        item.setId(rs.getInt("id"));
        Carrito c = new Carrito(); c.setId(rs.getInt("id_carrito"));
        item.setCarrito(c);
        int idProducto = rs.getInt("id_producto");
        if (!rs.wasNull()) { Producto p = new Producto(); p.setIdProducto(idProducto); item.setProducto(p); }
        int idPerfCust = rs.getInt("id_perf_cust");
        if (!rs.wasNull()) { PerfumeCustom pc = new PerfumeCustom(); pc.setIdPerfCust(idPerfCust); item.setPerfumeCustom(pc); }
        item.setCantidad(rs.getInt("cantidad"));
        return item;
    }
}
