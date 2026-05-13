package com.example.demo.dao;

import com.example.demo.entity.Fragancia;
import com.example.demo.entity.PerfumeCustom;
import com.example.demo.entity.PerfumeCustomFragancia;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerfumeCustomFraganciaDAO {

    public List<PerfumeCustomFragancia> findByPerfumeCustomId(Integer idPerfCust) throws SQLException {
        String sql = "SELECT id_perf_cust, id_fragancia, orden FROM perf_cust_fragancia " +
                     "WHERE id_perf_cust = ? ORDER BY orden NULLS LAST";
        List<PerfumeCustomFragancia> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPerfCust);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private PerfumeCustomFragancia mapRow(ResultSet rs) throws SQLException {
        PerfumeCustomFragancia pcf = new PerfumeCustomFragancia();
        pcf.setIdPerfCust(rs.getInt("id_perf_cust"));
        pcf.setIdFragancia(rs.getInt("id_fragancia"));
        int orden = rs.getInt("orden");
        pcf.setOrden(rs.wasNull() ? null : orden);
        PerfumeCustom pc = new PerfumeCustom(); pc.setIdPerfCust(pcf.getIdPerfCust());
        pcf.setPerfumeCustom(pc);
        Fragancia f = new Fragancia(); f.setIdFragancia(pcf.getIdFragancia());
        pcf.setFragancia(f);
        return pcf;
    }
}
