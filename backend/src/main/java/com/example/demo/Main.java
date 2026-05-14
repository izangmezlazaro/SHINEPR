package com.example.demo;

import com.example.demo.filter.CorsFilter;
import com.example.demo.filter.JwtFilter;
import com.example.demo.servlet.*;
import com.example.demo.util.ConexionDB;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Punto de entrada de la aplicación.
 * Arranca un Tomcat embebido en el puerto 8080.
 *
 * Comando: java -jar target/shine-server.jar
 */
public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(createTempDir());

        // Conector HTTP en el puerto 8080
        Connector connector = new Connector();
        connector.setPort(PORT);
        tomcat.getService().addConnector(connector);

        // Contexto raíz "/"
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());

        // ── Filtros ──────────────────────────────────────────────────────────
        addFilter(ctx, "CorsFilter", CorsFilter.class, "/*");
        addFilter(ctx, "JwtFilter",  JwtFilter.class,  "/api/v1/*");

        // ── Servlets ─────────────────────────────────────────────────────────
        addServlet(ctx, tomcat, "AuthServlet",           AuthServlet.class,          "/api/v1/auth/*");
        addServlet(ctx, tomcat, "ProductoServlet",       ProductoServlet.class,      "/api/v1/productos/*");
        addServlet(ctx, tomcat, "CategoriaServlet",      CategoriaServlet.class,     "/api/v1/categorias/*");
        addServlet(ctx, tomcat, "CarritoServlet",        CarritoServlet.class,       "/api/v1/carrito/*");
        addServlet(ctx, tomcat, "PedidoServlet",         PedidoServlet.class,        "/api/v1/pedidos/*");
        addServlet(ctx, tomcat, "DireccionServlet",      DireccionServlet.class,     "/api/v1/direcciones/*");
        addServlet(ctx, tomcat, "EnvioServlet",          EnvioServlet.class,         "/api/v1/envios/*");
        addServlet(ctx, tomcat, "PagoServlet",           PagoServlet.class,          "/api/v1/pagos/*");
        addServlet(ctx, tomcat, "FraganciaServlet",      FraganciaServlet.class,     "/api/v1/fragancias/*");
        addServlet(ctx, tomcat, "FrascoServlet",         FrascoServlet.class,        "/api/v1/frascos/*");
        addServlet(ctx, tomcat, "PerfumeCustomServlet",  PerfumeCustomServlet.class, "/api/v1/perfumes-custom/*");
        addServlet(ctx, tomcat, "NotaOlfativaServlet",   NotaOlfativaServlet.class,  "/api/v1/notas-olfativas/*");
        addServlet(ctx, tomcat, "AnuncioServlet",        AnuncioServlet.class,          "/api/v1/anuncios/*");
        addServlet(ctx, tomcat, "FichajeServlet",        FichajeServlet.class,          "/api/v1/fichajes/*");
        addServlet(ctx, tomcat, "ReunionServlet",        ReunionServlet.class,          "/api/v1/reuniones/*");
        addServlet(ctx, tomcat, "UsuarioServlet",        UsuarioServlet.class,          "/api/v1/usuarios/*");
        addServlet(ctx, tomcat, "IntranetPedidoServlet", IntranetPedidoServlet.class,   "/api/v1/intranet/pedidos/*");
        addServlet(ctx, tomcat, "GenerarCustomServlet",  GenerarCustomServlet.class, "/api/generar-custom/*");

        runMigrations();

        tomcat.start();
        System.out.println("╔════════════════════════════════════════════╗");
        System.out.println("║  Shine Backend iniciado en el puerto " + PORT + "    ║");
        System.out.println("║  http://localhost:" + PORT + "/api/v1/            ║");
        System.out.println("╚════════════════════════════════════════════╝");
        tomcat.getServer().await();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static void addServlet(Context ctx, Tomcat tomcat, String name,
                                    Class<?> clazz, String pattern) {
        Tomcat.addServlet(ctx, name, clazz.getName());
        ctx.addServletMappingDecoded(pattern, name);
    }

    private static void addFilter(Context ctx, String name, Class<?> clazz, String pattern) {
        FilterDef fd = new FilterDef();
        fd.setFilterName(name);
        fd.setFilterClass(clazz.getName());
        ctx.addFilterDef(fd);

        FilterMap fm = new FilterMap();
        fm.setFilterName(name);
        fm.addURLPattern(pattern);
        ctx.addFilterMap(fm);
    }

    private static void runMigrations() {
        String[] scripts = {"intranet_migration.sql", "puntos_migration.sql", "frascos_migration.sql", "pedido_estado_migration.sql"};
        try (Connection conn = ConexionDB.getConnection()) {
            for (String script : scripts) {
                try (InputStream in = Main.class.getClassLoader().getResourceAsStream(script)) {
                    if (in == null) { System.err.println("Migration not found: " + script); continue; }
                    String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql);
                        System.out.println("Migration OK: " + script);
                    }
                } catch (Exception e) {
                    System.err.println("Migration warning (" + script + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Cannot run migrations: " + e.getMessage());
        }
    }

    private static String createTempDir() {
        File f = new File(System.getProperty("java.io.tmpdir"), "shine-tomcat");
        f.mkdirs();
        return f.getAbsolutePath();
    }
}
