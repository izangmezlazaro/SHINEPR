package com.example.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee db.properties y app.properties del classpath y expone sus valores.
 * Singleton inicializado en el primer acceso (thread-safe via class-loading).
 */
public final class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        loadFile("db.properties");
        loadFile("app.properties");
    }

    private AppConfig() {}

    private static void loadFile(String filename) {
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new IllegalStateException("No se encontró " + filename + " en el classpath");
            }
            PROPS.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar " + filename, e);
        }
    }

    public static String get(String key) {
        String value = System.getenv(toEnvKey(key));
        if (value != null && !value.isBlank()) return value;
        value = PROPS.getProperty(key);
        if (value == null) throw new IllegalStateException("Propiedad no encontrada: " + key);
        return value;
    }

    /** Convierte "db.url" → "DB_URL" para buscar en variables de entorno */
    private static String toEnvKey(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    // ── Constantes de conveniencia ─────────────────────────────────────────
    public static String dbUrl()           { return get("db.url"); }
    public static String dbUsername()      { return get("db.username"); }
    public static String dbPassword()      { return get("db.password"); }
    public static String jwtSecret()       { return get("jwt.secret"); }
    public static long   jwtExpirationMs() { return Long.parseLong(get("jwt.expiration.ms")); }
    public static String openaiApiKey()    { return get("openai.api.key"); }
}
