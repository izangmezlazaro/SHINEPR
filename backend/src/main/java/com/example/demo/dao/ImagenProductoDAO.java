package com.example.demo.dao;

import com.example.demo.entity.ImagenProducto;
import com.example.demo.entity.Producto;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImagenProductoDAO {

    public List<ImagenProducto> findByProductoId(Integer idProducto) throws SQLException {
        String sql = "SELECT id_imagen, id_producto, url, descripcion FROM imagen_producto WHERE id_producto = ?";
        List<ImagenProducto> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, idProducto));
            }
        }
        return list;
    }

    /** Carga imágenes para múltiples productos en una sola query (evita N+1). */
    public List<ImagenProducto> findByProductoIds(List<Integer> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return List.of();
        String placeholders = "?,".repeat(ids.size());
        placeholders = placeholders.substring(0, placeholders.length() - 1);
        String sql = "SELECT id_imagen, id_producto, url, descripcion FROM imagen_producto " +
                     "WHERE id_producto IN (" + placeholders + ")";
        List<ImagenProducto> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs, rs.getInt("id_producto")));
            }
        }
        return list;
    }

    private ImagenProducto mapRow(ResultSet rs, int idProducto) throws SQLException {
        ImagenProducto img = new ImagenProducto();
        img.setIdImagen(rs.getInt("id_imagen"));
        Producto p = new Producto();
        p.setIdProducto(idProducto);
        img.setProducto(p);
        img.setUrl(rs.getString("url"));
        img.setDescripcion(rs.getString("descripcion"));
        return img;
    }
}
