package com.example.demo.dto;

import java.util.List;

public class DashboardStatsDTO {

    private double totalRevenue;
    private double monthRevenue;
    private double lastMonthRevenue;
    private int    totalOrders;
    private int    activeOrders;
    private int    totalCustomers;

    private List<ChartPoint> revenueWeek;
    private List<ChartPoint> revenueMonth;
    private List<ChartPoint> revenueYear;

    private List<RecentOrder>   recentOrders;
    private List<TopProduct>    topProducts;
    private List<LowStockItem>  lowStockProducts;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public double getTotalRevenue()            { return totalRevenue; }
    public void   setTotalRevenue(double v)    { this.totalRevenue = v; }

    public double getMonthRevenue()            { return monthRevenue; }
    public void   setMonthRevenue(double v)    { this.monthRevenue = v; }

    public double getLastMonthRevenue()        { return lastMonthRevenue; }
    public void   setLastMonthRevenue(double v){ this.lastMonthRevenue = v; }

    public int  getTotalOrders()               { return totalOrders; }
    public void setTotalOrders(int v)          { this.totalOrders = v; }

    public int  getActiveOrders()              { return activeOrders; }
    public void setActiveOrders(int v)         { this.activeOrders = v; }

    public int  getTotalCustomers()            { return totalCustomers; }
    public void setTotalCustomers(int v)       { this.totalCustomers = v; }

    public List<ChartPoint> getRevenueWeek()             { return revenueWeek; }
    public void             setRevenueWeek(List<ChartPoint> v)  { this.revenueWeek = v; }

    public List<ChartPoint> getRevenueMonth()            { return revenueMonth; }
    public void             setRevenueMonth(List<ChartPoint> v) { this.revenueMonth = v; }

    public List<ChartPoint> getRevenueYear()             { return revenueYear; }
    public void             setRevenueYear(List<ChartPoint> v)  { this.revenueYear = v; }

    public List<RecentOrder>  getRecentOrders()              { return recentOrders; }
    public void               setRecentOrders(List<RecentOrder> v)  { this.recentOrders = v; }

    public List<TopProduct>   getTopProducts()               { return topProducts; }
    public void               setTopProducts(List<TopProduct> v)    { this.topProducts = v; }

    public List<LowStockItem> getLowStockProducts()              { return lowStockProducts; }
    public void               setLowStockProducts(List<LowStockItem> v) { this.lowStockProducts = v; }

    // ── Nested DTOs ──────────────────────────────────────────────────────────

    public static class ChartPoint {
        public String label;
        public double value;
        public ChartPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    public static class RecentOrder {
        public int    id;
        public String customerName;
        public String estado;
        public double total;
        public String fecha;
        public RecentOrder(int id, String customerName, String estado, double total, String fecha) {
            this.id           = id;
            this.customerName = customerName;
            this.estado       = estado;
            this.total        = total;
            this.fecha        = fecha;
        }
    }

    public static class TopProduct {
        public String nombre;
        public double revenue;
        public int    unitsSold;
        public TopProduct(String nombre, double revenue, int unitsSold) {
            this.nombre    = nombre;
            this.revenue   = revenue;
            this.unitsSold = unitsSold;
        }
    }

    public static class LowStockItem {
        public int    id;
        public String nombre;
        public int    stock;
        public LowStockItem(int id, String nombre, int stock) {
            this.id     = id;
            this.nombre = nombre;
            this.stock  = stock;
        }
    }
}
