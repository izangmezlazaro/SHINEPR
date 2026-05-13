package com.example.demo.dao;

import com.example.demo.entity.Fragancia;
import com.example.demo.entity.Frasco;
import com.example.demo.entity.PerfumeCustom;
import com.example.demo.entity.PerfumeCustomFragancia;
import com.example.demo.entity.Usuario;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PerfumeCustomDAO {

    public List<PerfumeCustom> findByUsuarioId(Integer idUsuario) throws SQLException {
        String sql = "SELECT id_perf_cust, id_usuario, id_frasco, nombre_personalizado, " +
                     "intensidad, precio_calculado FROM perfume_custom WHERE id_usuario = ?";
        List<PerfumeCustom> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<PerfumeCustom> findById(Integer id) throws SQLException {
        String sql = "SELECT id_perf_cust, id_usuario, id_frasco, nombre_personalizado, " +
                     "intensidad, precio_calculado FROM perfume_custom WHERE id_perf_cust = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Inserta PerfumeCustom y sus fragancias en una transacción. */
    public PerfumeCustom save(PerfumeCustom pc) throws SQLException {
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO perfume_custom (id_usuario, id_frasco, nombre_personalizado, " +
                             "intensidad, precio_calculado) VALUES (?,?,?,?,?) RETURNING id_perf_cust";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, pc.getUsuario().getId());
                    ps.setInt(2, pc.getFrasco().getIdFrasco());
                    ps.setString(3, pc.getNombrePersonalizado());
                    ps.setString(4, pc.getIntensidad());
                    ps.setBigDecimal(5, pc.getPrecioCalculado());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        pc.setIdPerfCust(rs.getInt(1));
                    }
                }
                // Insertar fragancias
                String sqlFrag = "INSERT INTO perf_cust_fragancia (id_perf_cust, id_fragancia, orden) VALUES (?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlFrag)) {
                    for (PerfumeCustomFragancia rel : pc.getFragancias()) {
                        ps.setInt(1, pc.getIdPerfCust());
                        ps.setInt(2, rel.getFragancia().getIdFragancia());
                        if (rel.getOrden() != null) ps.setInt(3, rel.getOrden());
                        else ps.setNull(3, Types.INTEGER);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return pc;
    }

    private PerfumeCustom mapRow(ResultSet rs) throws SQLException {
        PerfumeCustom pc = new PerfumeCustom();
        pc.setIdPerfCust(rs.getInt("id_perf_cust"));
        Usuario u = new Usuario(); u.setId(rs.getInt("id_usuario")); pc.setUsuario(u);
        Frasco f = new Frasco(); f.setIdFrasco(rs.getInt("id_frasco")); pc.setFrasco(f);
        pc.setNombrePersonalizado(rs.getString("nombre_personalizado"));
        pc.setIntensidad(rs.getString("intensidad"));
        pc.setPrecioCalculado(rs.getBigDecimal("precio_calculado"));
        return pc;
    }
}
