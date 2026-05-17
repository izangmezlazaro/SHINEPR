package com.example.demo.servlet;

import com.example.demo.dao.DashboardDAO;
import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * GET /api/v1/intranet/dashboard → KPIs y series temporales para el panel de administración
 */
@WebServlet(urlPatterns = "/api/v1/intranet/dashboard/*", name = "IntranetDashboardServlet")
public class IntranetDashboardServlet extends HttpServlet {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpUtil.writeJson(resp, 200, dashboardDAO.getStats());
        } catch (Exception e) {
            HttpUtil.writeError(resp, 500, "Error interno: " + e.getMessage());
        }
    }
}
