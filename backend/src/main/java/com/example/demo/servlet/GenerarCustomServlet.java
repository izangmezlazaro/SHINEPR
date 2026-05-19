package com.example.demo.servlet;

import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * POST /api/generar-custom
 * Generates a luxury perfume AI image using OpenAI gpt-image-1-mini.
 */
@WebServlet(urlPatterns = "/api/generar-custom", name = "GenerarCustomServlet")
public class GenerarCustomServlet extends HttpServlet {

    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY") != null
            ? System.getenv("OPENAI_API_KEY")
            : "AQUI VA LA API KEY DE OPENAI"; // <--
                                              // PEGA
                                              // TU
                                              // API
                                              // KEY
                                              // AQUÍ

    private static final String MODEL = "gpt-image-1-mini";
    private static final int CONNECT_MS = 15_000; // 15 s
    private static final int READ_MS = 120_000; // 2 min – generation can be slow

    // Rate Limiting: IP -> Timestamp of last request
    private static final ConcurrentHashMap<String, Long> rateLimiter = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_MS = 20_000; // 20 seconds per IP

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String clientIp = req.getRemoteAddr();
        long now = System.currentTimeMillis();
        long lastRequest = rateLimiter.getOrDefault(clientIp, 0L);

        if (now - lastRequest < RATE_LIMIT_MS) {
            HttpUtil.writeError(resp, 429, "Please wait " + ((RATE_LIMIT_MS - (now - lastRequest)) / 1000)
                    + " seconds before generating another design.");
            return;
        }
        rateLimiter.put(clientIp, now);
        if (OPENAI_API_KEY == null || OPENAI_API_KEY.trim().isEmpty()) {
            System.err.println(">> OPENAI_API_KEY IS MISSING!");
            HttpUtil.writeError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "The OpenAI API key is not configured in the backend.");
            return;
        }

        try {
            // ── Read parameters (application/x-www-form-urlencoded) ──────────
            String tipo = nvl(req.getParameter("tipo"), "Eau de Parfum");
            String base = nvl(req.getParameter("base"), "");
            String notas = nvl(req.getParameter("notas"), "floral and woody");
            String formaElegida = nvl(req.getParameter("formaElegida"), "sleek glass");
            String nombreUsuario = nvl(req.getParameter("nombreUsuario"), "Shine");
            String nombreEtiqueta = stripLabelMarkers(nombreUsuario);
            if (nombreEtiqueta.isEmpty()) {
                nombreEtiqueta = "Shine";
            }

            System.out.println("=== SHINE AI GENERATOR ===");
            System.out.println("Model     : " + MODEL);
            System.out.println("Type      : " + tipo);
            System.out.println("Base      : " + base);
            System.out.println("Notes     : " + notas);
            System.out.println("Shape     : " + formaElegida);
            System.out.println("Name      : " + nombreUsuario);

            // Compact prompt + strict label copy (user casing / punctuation preserved
            // in-image)
            String notasClause = notas.isEmpty() ? "" : ", infused with " + notas + " notes";
            String baseClause = base.isEmpty() ? "" : ", base " + base;
            String prompt = "Ultra-premium luxury perfume editorial photograph. "
                    + "Subject: a " + formaElegida + " " + tipo + " perfume bottle standing upright "
                    + baseClause
                    + notasClause
                    + ". "
                    + "Beside the bottle, its matching luxury gift box (rectangular matte black with rose gold foil embossing, slightly open, lid resting at an angle). "
                    + "Both bottle and box display the label: <<<" + nombreEtiqueta + ">>> "
                    + "(reproduce label text EXACTLY — same capitals, spaces, accents, punctuation; no translation, no autocorrect). "
                    + "Scene: dark cinematic studio, black marble surface, single overhead spotlight and subtle rim lighting on the glass, "
                    + "scattered dried botanicals in soft nude and white tones at the base, delicate smoke wisps rising. "
                    + "Rose gold foil accents on label. Photorealistic, 8K render, macro depth of field, ultra-sharp glass reflections. "
                    + "Professional luxury fragrance brand campaign style. No people, no text except the exact label.";

            JsonObject openAiRequest = new JsonObject();
            openAiRequest.addProperty("model", MODEL);
            openAiRequest.addProperty("prompt", prompt);
            openAiRequest.addProperty("n", 1);
            openAiRequest.addProperty("size", "1024x1024");
            String body = new Gson().toJson(openAiRequest);

            System.out.println("Prompt    : " + prompt);

            // ── Call OpenAI API ───────────────────────────────────────────────
            URL apiUrl = new URL("https://api.openai.com/v1/images/generations");
            HttpURLConnection con = (HttpURLConnection) apiUrl.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
            con.setDoOutput(true);
            con.setConnectTimeout(CONNECT_MS);
            con.setReadTimeout(READ_MS);

            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes("utf-8"));
            }

            int status = con.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? con.getInputStream()
                    : con.getErrorStream();

            String responseStr;
            try (Scanner scanner = new Scanner(is, "utf-8")) {
                responseStr = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            System.out.println(">> OPENAI STATUS : " + status);
            if (status != 200) {
                System.err.println(">> OPENAI ERROR  : " + responseStr);
            }

            // ── Return result to frontend ─────────────────────────────────────
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            if (status == 200) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(ensureInlineBase64Image(responseStr));
            } else {
                String safeDetail = responseStr;
                if (safeDetail.length() > 1800) {
                    safeDetail = safeDetail.substring(0, 1800) + "…";
                }
                safeDetail = safeDetail.replace("\\", "\\\\").replace("\"", "\\\"");
                int proxyStatus = (status == 401 || status == 403) ? HttpServletResponse.SC_UNAUTHORIZED
                        : (status == 400 || status == 422) ? HttpServletResponse.SC_BAD_REQUEST
                                : HttpServletResponse.SC_BAD_GATEWAY;
                resp.setStatus(proxyStatus);
                resp.getWriter().write(
                        "{\"error\":\"OpenAI returned " + status + "\",\"detail\":\"" + safeDetail + "\"}");
            }

        } catch (Exception e) {
            System.err.println(">> CRASH IN GenerarCustomServlet!");
            e.printStackTrace();
            HttpUtil.writeError(resp, 500, "Internal error generating AI image: " + e.getMessage());
        }
    }

    /**
     * Returns {@code value} if non-null and non-blank, otherwise {@code fallback}.
     */
    private static String nvl(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : fallback;
    }

    /**
     * Prevent user text from breaking the <<< >>> label delimiters in the prompt.
     */
    private static String stripLabelMarkers(String name) {
        if (name == null) {
            return "";
        }
        return name.replace("<<<", "").replace(">>>", "").trim();
    }

    /**
     * If OpenAI returns only a temporary {@code url}, the browser often cannot show
     * it in {@code <img>}
     * (CORS / referrer / expiry). Fetch the bytes here and return JSON with
     * {@code b64_json} populated.
     */
    private static String ensureInlineBase64Image(String openAiJson) {
        try {
            JsonObject root = JsonParser.parseString(openAiJson).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");
            if (data == null || data.isEmpty()) {
                return openAiJson;
            }
            if (!data.get(0).isJsonObject()) {
                return openAiJson;
            }
            JsonObject first = data.get(0).getAsJsonObject();
            if (hasNonBlank(first, "b64_json")) {
                return openAiJson;
            }
            if (!hasNonBlank(first, "url")) {
                return openAiJson;
            }
            String imageUrl = first.get("url").getAsString().trim();
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return openAiJson;
            }
            byte[] bytes = fetchUrlBytes(imageUrl);
            if (bytes.length == 0) {
                return openAiJson;
            }
            first.remove("url");
            first.addProperty("b64_json", Base64.getEncoder().encodeToString(bytes));
            return new Gson().toJson(root);
        } catch (Exception e) {
            System.err.println("ensureInlineBase64Image: " + e.getMessage());
            return openAiJson;
        }
    }

    private static boolean hasNonBlank(JsonObject o, String field) {
        if (o == null || !o.has(field) || o.get(field).isJsonNull()) {
            return false;
        }
        String s = o.get(field).getAsString();
        return s != null && !s.trim().isEmpty();
    }

    private static byte[] fetchUrlBytes(String imageUrl) throws IOException {
        URL u = new URL(imageUrl);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(CONNECT_MS);
        c.setReadTimeout(READ_MS);
        c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "ShinePerfume/1.0 (+https://shine.local)");
        int code = c.getResponseCode();
        if (code < 200 || code >= 300) {
            InputStream err = c.getErrorStream();
            if (err != null) {
                try (InputStream es = err) {
                    es.readAllBytes();
                }
            }
            return new byte[0];
        }
        try (InputStream in = c.getInputStream()) {
            return in.readAllBytes();
        } finally {
            c.disconnect();
        }
    }
}