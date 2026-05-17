package com.example.demo.dao;

import com.example.demo.dto.ActivityItemDTO;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Agrega eventos de actividad reciente de múltiples tablas mediante UNION.
 * Para añadir un nuevo tipo de evento, basta con añadir otro bloque SELECT
 * al UNION sin tocar el servlet ni el frontend.
 */
public class ActividadDAO {

    private static final int LIMIT = 20;

    public List<ActivityItemDTO> getActividadReciente() throws SQLException {
        String sql =
            // ── Pedidos nuevos ───────────────────────────────────────────────
            "SELECT 'pedido'           AS tipo," +
            "       p.id_pedido        AS ref_id," +
            "       CONCAT('Nuevo pedido #', p.id_pedido, ' de ', u.nombre) AS descripcion," +
            "       u.nombre           AS usuario," +
            "       p.fecha            AS ts," +
            "       '#16A34A'          AS color," +
            "       'order'            AS icono" +
            " FROM pedido p JOIN usuario u ON p.id_usuario = u.id" +
            " WHERE p.fecha >= NOW() - INTERVAL '30 days'" +
            " UNION ALL" +
            // ── Fichajes (entradas) ──────────────────────────────────────────
            " SELECT 'fichaje'          AS tipo," +
            "        f.id               AS ref_id," +
            "        CONCAT(f.empleado_nombre, ' fichó entrada') AS descripcion," +
            "        f.empleado_nombre  AS usuario," +
            "        f.hora_entrada     AS ts," +
            "        '#2563EB'          AS color," +
            "        'clock'            AS icono" +
            " FROM fichaje f" +
            " WHERE f.hora_entrada >= NOW() - INTERVAL '7 days'" +
            " UNION ALL" +
            // ── Fichajes (salidas) ───────────────────────────────────────────
            " SELECT 'fichaje'          AS tipo," +
            "        f.id               AS ref_id," +
            "        CONCAT(f.empleado_nombre, ' fichó salida')  AS descripcion," +
            "        f.empleado_nombre  AS usuario," +
            "        f.hora_salida      AS ts," +
            "        '#6366F1'          AS color," +
            "        'clock-out'        AS icono" +
            " FROM fichaje f" +
            " WHERE f.hora_salida IS NOT NULL AND f.hora_salida >= NOW() - INTERVAL '7 days'" +
            " UNION ALL" +
            // ── Anuncios publicados ──────────────────────────────────────────
            " SELECT 'anuncio'          AS tipo," +
            "        a.id               AS ref_id," +
            "        CONCAT('Anuncio: ', a.titulo) AS descripcion," +
            "        a.autor            AS usuario," +
            "        a.fecha            AS ts," +
            "        '#D4919A'          AS color," +
            "        'megaphone'        AS icono" +
            " FROM anuncio a" +
            " WHERE a.fecha >= NOW() - INTERVAL '30 days'" +
            " ORDER BY ts DESC NULLS LAST" +
            " LIMIT " + LIMIT;

        List<ActivityItemDTO> list = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("ts");
                String isoTs = ts != null ? ts.toLocalDateTime().toString() : null;
                list.add(new ActivityItemDTO(
                    rs.getString("tipo"),
                    rs.getString("descripcion"),
                    rs.getString("usuario"),
                    isoTs,
                    rs.getString("color"),
                    rs.getString("icono"),
                    rs.getInt("ref_id")
                ));
            }
        }
        return list;
    }
}
