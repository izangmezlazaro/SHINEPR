package com.example.demo.dao;

import com.example.demo.entity.Carrito;
import com.example.demo.entity.CarritoItem;
import com.example.demo.entity.Categoria;
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

    public void updateCantidad(Integer id, int cantidad) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE carrito_item SET cantidad = ? WHERE id = ?")) {
            ps.setInt(1, cantidad);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public Optional<CarritoItem> findByCarritoIdAndProductoId(Integer idCarrito, Integer idProducto) throws SQLException {
        String sql = "SELECT id, id_carrito, id_producto, id_perf_cust, cantidad " +
                     "FROM carrito_item WHERE id_carrito = ? AND id_producto = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.setInt(2, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<CarritoItem> findByCarritoIdAndPerfCustId(Integer idCarrito, Integer idPerfCust) throws SQLException {
        String sql = "SELECT id, id_carrito, id_producto, id_perf_cust, cantidad " +
                     "FROM carrito_item WHERE id_carrito = ? AND id_perf_cust = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.setInt(2, idPerfCust);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public void deleteByCarritoId(Integer idCarrito) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM carrito_item WHERE id_carrito = ?")) {
            ps.setInt(1, idCarrito);
            ps.executeUpdate();
        }
    }

    public List<CarritoItem> findByCarritoIdEnriquecido(Integer idCarrito) throws SQLException {
        String sql =
            "SELECT ci.id, ci.id_carrito, ci.id_producto, ci.id_perf_cust, ci.cantidad, " +
            "p.sku, p.nombre AS prod_nombre, p.descripcion, p.ingredientes, p.modo_uso, " +
            "p.precio, p.stock, p.genero, p.tipo_fragancia, p.id_categoria, cat.nombre AS cat_nombre, " +
            "pc.nombre_personalizado, pc.intensidad, pc.precio_calculado, " +
            "img1.url AS imagen_url " +
            "FROM carrito_item ci " +
            "LEFT JOIN producto p ON ci.id_producto = p.id_producto " +
            "LEFT JOIN categoria cat ON p.id_categoria = cat.id_categoria " +
            "LEFT JOIN perfume_custom pc ON ci.id_perf_cust = pc.id_perf_cust " +
            "LEFT JOIN LATERAL (" +
            "  SELECT url FROM imagen_producto WHERE id_producto = ci.id_producto ORDER BY id_imagen LIMIT 1" +
            ") img1 ON ci.id_producto IS NOT NULL " +
            "WHERE ci.id_carrito = ?";
        List<CarritoItem> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowEnriquecido(rs));
            }
        }
        return list;
    }

    private CarritoItem mapRowEnriquecido(ResultSet rs) throws SQLException {
        CarritoItem item = new CarritoItem();
        item.setId(rs.getInt("id"));
        Carrito c = new Carrito(); c.setId(rs.getInt("id_carrito"));
        item.setCarrito(c);
        item.setCantidad(rs.getInt("cantidad"));

        int idProducto = rs.getInt("id_producto");
        if (!rs.wasNull()) {
            Producto p = new Producto();
            p.setIdProducto(idProducto);
            p.setSku(rs.getString("sku"));
            p.setNombre(rs.getString("prod_nombre"));
            p.setDescripcion(rs.getString("descripcion"));
            p.setIngredientes(rs.getString("ingredientes"));
            p.setModoUso(rs.getString("modo_uso"));
            p.setPrecio(rs.getBigDecimal("precio"));
            p.setStock(rs.getInt("stock"));
            p.setGenero(rs.getString("genero"));
            p.setTipoFragancia(rs.getString("tipo_fragancia"));
            p.setCategoria(new Categoria(rs.getInt("id_categoria"), rs.getString("cat_nombre")));
            item.setProducto(p);
        }

        int idPerfCust = rs.getInt("id_perf_cust");
        if (!rs.wasNull()) {
            PerfumeCustom pc = new PerfumeCustom();
            pc.setIdPerfCust(idPerfCust);
            pc.setNombrePersonalizado(rs.getString("nombre_personalizado"));
            pc.setIntensidad(rs.getString("intensidad"));
            pc.setPrecioCalculado(rs.getBigDecimal("precio_calculado"));
            item.setPerfumeCustom(pc);
        }

        item.setImagenUrl(rs.getString("imagen_url"));
        return item;
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
