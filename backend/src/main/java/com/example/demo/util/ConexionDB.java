package com.example.demo.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConexionDB {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.dbUrl());
        config.setUsername(AppConfig.dbUsername());
        config.setPassword(AppConfig.dbPassword());
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        dataSource = new HikariDataSource(config);
    }

    private ConexionDB() {}

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
