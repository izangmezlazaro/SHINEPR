package com.example.demo.servlet;

import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * POST /api/generar-custom
 * Generates a luxury perfume AI image using OpenAI gpt-image-1-mini.
 */
@WebServlet(urlPatterns = "/api/generar-custom", name = "GenerarCustomServlet")
public class GenerarCustomServlet extends HttpServlet {

    private static final String OPENAI_API_KEY = "";

    private static final String MODEL = "gpt-image-1-mini";
    private static final int CONNECT_MS = 15_000; // 15 s
    private static final int READ_MS = 120_000; // 2 min – generation can be slow

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // ── Read parameters (application/x-www-form-urlencoded) ──────────
            String tipo = nvl(req.getParameter("tipo"), "Eau de Parfum");
            String base = nvl(req.getParameter("base"), "");
            String notas = nvl(req.getParameter("notas"), "floral and woody");
            String formaElegida = nvl(req.getParameter("formaElegida"), "sleek glass");
            String nombreUsuario = nvl(req.getParameter("nombreUsuario"), "Shine");

            System.out.println("=== SHINE AI GENERATOR ===");
            System.out.println("Model     : " + MODEL);
            System.out.println("Type      : " + tipo);
            System.out.println("Base      : " + base);
            System.out.println("Notes     : " + notas);
            System.out.println("Shape     : " + formaElegida);
            System.out.println("Name      : " + nombreUsuario);

            // ── Build the prompt ─────────────────────────────────────────────
            String prompt = "Luxury perfume product photography on a dark, moody background. "
                    + "Center: a " + formaElegida + " glass perfume bottle with a translucent liquid. "
                    + "Beside it, a matching matte-finish premium box. "
                    + "Both display the name '" + nombreUsuario + "' in minimalist gold serif typography. "
                    + "Fragrance style: " + tipo + ". "
                    + (!notas.isEmpty() ? "Scent notes: " + notas + ". " : "")
                    + "Cinematic studio lighting, realistic glass reflections and soft shadows. "
                    + "8K photorealistic render, no watermarks, no extra text.";

            String safePrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"");

            String body = "{"
                    + "\"model\":\"" + MODEL + "\","
                    + "\"prompt\":\"" + safePrompt + "\","
                    + "\"n\":1,"
                    + "\"size\":\"1024x1024\""
                    + "}";

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
                resp.getWriter().write(responseStr);
            } else {
                // Return error as JSON so the frontend can display a proper message
                String safeDetail = responseStr.replace("\\", "\\\\").replace("\"", "\\\"");
                resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
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
}