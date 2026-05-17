package com.example.demo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Wrapper de Gson con configuración estándar para la API:
 * - LocalDateTime serializado como ISO-8601 string
 * - BigDecimal serializado como número (no string)
 */
public final class JsonUtil {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter D_FMT  = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(DT_FMT)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                            LocalDateTime.parse(json.getAsString(), DT_FMT))
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) ->
                            new JsonPrimitive(src.format(D_FMT)))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) ->
                            LocalDate.parse(json.getAsString(), D_FMT))
            .serializeNulls()
            .create();

    private JsonUtil() {}

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }
}
