package com.example.demo.dao;

import com.example.demo.entity.Categoria;
import com.example.demo.entity.Producto;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {

    private static final String SELECT_WITH_CAT =
        "SELECT p.id_producto, p.sku, p.nombre, p.descripcion, p.ingredientes, p.modo_uso, " +
        "p.precio, p.stock, p.genero, p.tipo_fragancia, p.id_subcategoria, " +
        "c.id_categoria, c.nombre AS cat_nombre " +
        "FROM producto p JOIN categoria c ON p.id_categoria = c.id_categoria ";

    public List<Producto> findAll() throws SQLException {
        List<Producto> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_WITH_CAT + "ORDER BY p.id_producto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Producto> findById(Integer id) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_WITH_CAT + "WHERE p.id_producto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Producto> findBySku(String sku) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_WITH_CAT + "WHERE p.sku = ?")) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Producto save(Producto p) throws SQLException {
        return (p.getIdProducto() == null) ? insert(p) : update(p);
    }

    private Producto insert(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (sku, nombre, descripcion, ingredientes, modo_uso, " +
                     "precio, stock, genero, tipo_fragancia, id_categoria, id_subcategoria) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?) RETURNING id_producto";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.setIdProducto(rs.getInt(1));
            }
        }
        return p;
    }

    private Producto update(Producto p) throws SQLException {
        String sql = "UPDATE producto SET sku=?, nombre=?, descripcion=?, ingredientes=?, modo_uso=?, " +
                     "precio=?, stock=?, genero=?, tipo_fragancia=?, id_categoria=?, id_subcategoria=? " +
                     "WHERE id_producto=?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParams(ps, p);
            ps.setInt(12, p.getIdProducto());
            ps.executeUpdate();
        }
        return p;
    }

    private void setParams(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getSku());
        ps.setString(2, p.getNombre());
        ps.setString(3, p.getDescripcion());
        ps.setString(4, p.getIngredientes());
        ps.setString(5, p.getModoUso());
        ps.setBigDecimal(6, p.getPrecio());
        ps.setInt(7, p.getStock() == null ? 0 : p.getStock());
        ps.setString(8, p.getGenero());
        ps.setString(9, p.getTipoFragancia());
        ps.setInt(10, p.getCategoria().getIdCategoria());
        if (p.getIdSubcategoria() != null) ps.setInt(11, p.getIdSubcategoria());
        else ps.setNull(11, java.sql.Types.INTEGER);
    }

    public void decrementarStock(Integer idProducto, int cantidad, Connection conn) throws SQLException {
        String sql = "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Stock insuficiente para el producto con id " + idProducto);
            }
        }
    }

    public void delete(Integer id) throws SQLException {
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Producto mapRow(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(rs.getInt("id_categoria"), rs.getString("cat_nombre"));
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setSku(rs.getString("sku"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setIngredientes(rs.getString("ingredientes"));
        p.setModoUso(rs.getString("modo_uso"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setGenero(rs.getString("genero"));
        p.setTipoFragancia(rs.getString("tipo_fragancia"));
        p.setCategoria(cat);
        int idSub = rs.getInt("id_subcategoria");
        p.setIdSubcategoria(rs.wasNull() ? null : idSub);
        return p;
    }
}
