package com.example.demo.dao;

import com.example.demo.dto.DashboardStatsDTO;
import com.example.demo.dto.DashboardStatsDTO.*;
import com.example.demo.util.ConexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class DashboardDAO {

    private static final String[] DIAS_ES   = {"Lun","Mar","Mié","Jue","Vie","Sáb","Dom"};
    private static final String[] MESES_ES  = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};

    public DashboardStatsDTO getStats() throws SQLException {
        try (Connection conn = ConexionDB.getConnection()) {
            DashboardStatsDTO stats = new DashboardStatsDTO();
            setSummaryStats(conn, stats);
            stats.setRevenueWeek(revenueByDay(conn));
            stats.setRevenueMonth(revenueByWeek(conn));
            stats.setRevenueYear(revenueByMonth(conn));
            stats.setRecentOrders(recentOrders(conn));
            stats.setTopProducts(topProducts(conn));
            stats.setLowStockProducts(lowStock(conn));
            stats.setRecentAnuncios(recentAnuncios(conn));
            return stats;
        }
    }

    // ── Summary KPIs ─────────────────────────────────────────────────────────

    private void setSummaryStats(Connection conn, DashboardStatsDTO stats) throws SQLException {
        String sql =
            "SELECT " +
            "  COALESCE(SUM(total), 0) AS total_revenue," +
            "  COUNT(*) AS total_orders," +
            "  COUNT(DISTINCT id_usuario) AS total_customers," +
            "  COUNT(CASE WHEN estado IN ('pendiente','procesando') THEN 1 END) AS active_orders," +
            "  COALESCE(SUM(CASE WHEN fecha >= DATE_TRUNC('month', CURRENT_DATE) THEN total ELSE 0 END), 0) AS month_revenue," +
            "  COALESCE(SUM(CASE WHEN fecha >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '1 month'" +
            "                     AND fecha < DATE_TRUNC('month', CURRENT_DATE) THEN total ELSE 0 END), 0) AS last_month_revenue" +
            " FROM pedido";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.setTotalRevenue(rs.getDouble("total_revenue"));
                stats.setTotalOrders(rs.getInt("total_orders"));
                stats.setTotalCustomers(rs.getInt("total_customers"));
                stats.setActiveOrders(rs.getInt("active_orders"));
                stats.setMonthRevenue(rs.getDouble("month_revenue"));
                stats.setLastMonthRevenue(rs.getDouble("last_month_revenue"));
            }
        }
    }

    // ── Revenue by day (last 7 days) ─────────────────────────────────────────

    private List<ChartPoint> revenueByDay(Connection conn) throws SQLException {
        String sql =
            "SELECT DATE(fecha) AS day, SUM(total) AS revenue" +
            " FROM pedido" +
            " WHERE fecha >= CURRENT_DATE - 6" +
            " GROUP BY DATE(fecha)" +
            " ORDER BY day";

        Map<LocalDate, Double> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate d = rs.getDate("day").toLocalDate();
                map.put(d, rs.getDouble("revenue"));
            }
        }

        List<ChartPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int dow = d.getDayOfWeek().getValue() - 1; // 0=Mon … 6=Sun
            points.add(new ChartPoint(DIAS_ES[dow], map.getOrDefault(d, 0.0)));
        }
        return points;
    }

    // ── Revenue by week (last 4 weeks) ───────────────────────────────────────

    private List<ChartPoint> revenueByWeek(Connection conn) throws SQLException {
        String sql =
            "SELECT DATE_TRUNC('week', fecha)::date AS week_start, SUM(total) AS revenue" +
            " FROM pedido" +
            " WHERE fecha >= CURRENT_DATE - 27" +
            " GROUP BY DATE_TRUNC('week', fecha)" +
            " ORDER BY week_start";

        Map<LocalDate, Double> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate w = rs.getDate("week_start").toLocalDate();
                map.put(w, rs.getDouble("revenue"));
            }
        }

        // Build 4 week slots ending this week
        List<ChartPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        // Monday of current week
        LocalDate thisMonday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        for (int i = 3; i >= 0; i--) {
            LocalDate weekStart = thisMonday.minusWeeks(i);
            double rev = map.getOrDefault(weekStart, 0.0);
            points.add(new ChartPoint("Sem " + (4 - i), rev));
        }
        return points;
    }

    // ── Revenue by month (last 12 months) ────────────────────────────────────

    private List<ChartPoint> revenueByMonth(Connection conn) throws SQLException {
        String sql =
            "SELECT DATE_TRUNC('month', fecha)::date AS month_start, SUM(total) AS revenue" +
            " FROM pedido" +
            " WHERE fecha >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '11 months'" +
            " GROUP BY DATE_TRUNC('month', fecha)" +
            " ORDER BY month_start";

        Map<LocalDate, Double> map = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate m = rs.getDate("month_start").toLocalDate();
                map.put(m, rs.getDouble("revenue"));
            }
        }

        List<ChartPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate firstMonth = today.withDayOfMonth(1).minusMonths(11);
        for (int i = 0; i < 12; i++) {
            LocalDate month = firstMonth.plusMonths(i);
            String label = MESES_ES[month.getMonthValue() - 1];
            points.add(new ChartPoint(label, map.getOrDefault(month, 0.0)));
        }
        return points;
    }

    // ── Recent orders ────────────────────────────────────────────────────────

    private List<RecentOrder> recentOrders(Connection conn) throws SQLException {
        String sql =
            "SELECT p.id_pedido, p.estado, p.total, p.fecha, u.nombre AS u_nombre" +
            " FROM pedido p" +
            " JOIN usuario u ON p.id_usuario = u.id" +
            " ORDER BY p.fecha DESC LIMIT 5";

        List<RecentOrder> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha");
                String fechaStr = ts != null ? ts.toLocalDateTime().toString() : "";
                list.add(new RecentOrder(
                    rs.getInt("id_pedido"),
                    rs.getString("u_nombre"),
                    rs.getString("estado"),
                    rs.getDouble("total"),
                    fechaStr
                ));
            }
        }
        return list;
    }

    // ── Top products by revenue ───────────────────────────────────────────────

    private List<TopProduct> topProducts(Connection conn) throws SQLException {
        String sql =
            "SELECT p.nombre," +
            "       SUM(dp.cantidad * dp.precio_unitario) AS total_revenue," +
            "       SUM(dp.cantidad) AS units_sold" +
            " FROM detalle_pedido dp" +
            " JOIN producto p ON dp.id_producto = p.id_producto" +
            " GROUP BY p.id_producto, p.nombre" +
            " ORDER BY total_revenue DESC LIMIT 5";

        List<TopProduct> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new TopProduct(
                    rs.getString("nombre"),
                    rs.getDouble("total_revenue"),
                    rs.getInt("units_sold")
                ));
            }
        }
        return list;
    }

    // ── Recent announcements ─────────────────────────────────────────────────

    private List<DashboardStatsDTO.RecentAnuncio> recentAnuncios(Connection conn) throws SQLException {
        String sql =
            "SELECT id, titulo, tag, fecha FROM anuncio" +
            " WHERE fecha >= NOW() - INTERVAL '7 days'" +
            " ORDER BY fecha DESC LIMIT 3";

        List<DashboardStatsDTO.RecentAnuncio> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("fecha");
                String fechaStr = ts != null ? ts.toLocalDateTime().toString() : "";
                list.add(new DashboardStatsDTO.RecentAnuncio(
                    rs.getInt("id"),
                    rs.getString("titulo"),
                    rs.getString("tag"),
                    fechaStr
                ));
            }
        }
        return list;
    }

    // ── Low stock products ────────────────────────────────────────────────────

    private List<DashboardStatsDTO.LowStockItem> lowStock(Connection conn) throws SQLException {
        String sql =
            "SELECT id_producto, nombre, stock" +
            " FROM producto" +
            " WHERE stock IS NOT NULL AND stock < 20" +
            " ORDER BY stock ASC LIMIT 5";

        List<DashboardStatsDTO.LowStockItem> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new DashboardStatsDTO.LowStockItem(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getInt("stock")
                ));
            }
        }
        return list;
    }
}
