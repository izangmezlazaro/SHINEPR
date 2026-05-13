package com.example.demo.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton que provee conexiones JDBC directas a PostgreSQL.
 * No usa pool de conexiones — cada llamada abre una nueva conexión.
 * El llamador es responsable de cerrarla (try-with-resources).
 *
 * Uso:
 *   try (Connection conn = ConexionDB.getConnection()) { ... }
 */
public final class ConexionDB {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver PostgreSQL no encontrado", e);
        }
        URL      = AppConfig.dbUrl();
        USER     = AppConfig.dbUsername();
        PASSWORD = AppConfig.dbPassword();
    }

    private ConexionDB() {}

    /**
     * Abre y devuelve una conexión JDBC.
     * IMPORTANTE: usar siempre en try-with-resources para garantizar cierre.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
