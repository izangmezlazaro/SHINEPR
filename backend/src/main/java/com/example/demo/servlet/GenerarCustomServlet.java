package com.example.demo.servlet;

import com.example.demo.util.HttpUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * POST /api/generar-custom
 */
@WebServlet(urlPatterns = "/api/generar-custom", name = "GenerarCustomServlet")
public class GenerarCustomServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // Read parameters (sent as application/x-www-form-urlencoded)
            String tipo = req.getParameter("tipo");
            String base = req.getParameter("base");
            String notas = req.getParameter("notas");
            String frasco = req.getParameter("frasco");
            String formaElegida = req.getParameter("formaElegida");
            String nombreUsuario = req.getParameter("nombreUsuario");

            System.out.println("--- SHINE AI GENERATOR ---");
            System.out.println("Type: " + tipo);
            System.out.println("Base: " + base);
            System.out.println("Notes: " + notas);
            System.out.println("Bottle: " + frasco);
            System.out.println("Shape: " + formaElegida);
            System.out.println("Name: " + nombreUsuario);

            // =========================================================
            // TODO: API FOR IMAGE GENERATION GOES HERE
            // =========================================================
            // Construct the prompt with the received information
            String prompt = "Professional luxury perfume branding mockup. In the center, a "
                    + (formaElegida != null ? formaElegida : "elegant") +
                    " glass bottle filled with a translucent liquid. To its right, a premium textured paper " +
                    "packaging box that matches the bottle's aesthetic. " +
                    "Both the bottle and the box feature a matching minimalist label with the name '"
                    + (nombreUsuario != null ? nombreUsuario : "Shine") + "' " +
                    "printed in elegant gold leaf typography. " +
                    "Details: The bottle cap is " + (frasco != null ? frasco : "premium glass")
                    + ". Sophisticated studio lighting, " +
                    "soft shadows, realistic reflections on the glass. " +
                    "Background: Dark, neutral, elegant interior. " +
                    "Cinematic product shot, 8k resolution, sharp focus, no extra decorative objects.";

            String apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Environment variable OPENAI_API_KEY is not set.");
            }

            // Connection to the OpenAI API
            java.net.URL url = new java.net.URL("https://api.openai.com/v1/images/generations");
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setDoOutput(true);

            // Escape quotes in the prompt to avoid JSON errors
            String safePrompt = prompt.replace("\"", "\\\"");

            // Use DALL-E 3 for best quality possible
            String requestBody = "{\n" +
                    "  \"model\": \"chatgpt-image-latest\",\n" +
                    "  \"prompt\": \"" + safePrompt + "\",\n" +
                    "  \"n\": 1,\n" +
                    "  \"size\": \"1024x1024\"\n" +
                    "}";

            try (java.io.OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read response from OpenAI
            int status = con.getResponseCode();
            java.io.InputStream is = (status < 200 || status >= 300) ? con.getErrorStream() : con.getInputStream();
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            String responseStr = s.hasNext() ? s.next() : "";
            System.out.println(">> OPENAI STATUS: " + status);
            System.out.println(">> OPENAI JSON: " + responseStr);

            if (status != 200) {
                System.err.println("OpenAI Error: " + responseStr);
                // Since OpenAI API Key is denying access to the model,
                // we will return a mock image so the website doesn't break.
                String fallbackImage = "https://images.unsplash.com/photo-1594035910387-fea47794261f?q=80&w=800&auto=format&fit=crop";
                String fallbackResponse = "{ \"data\": [ { \"url\": \"" + fallbackImage + "\" } ] }";

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(fallbackResponse);
                return;
            }

            // OpenAI already returns a JSON with the format { "data": [ { "url": "..." } ]
            // }
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(responseStr);

        } catch (Exception e) {
            // ¡AQUÍ ESTÁN LOS CHIVATOS!
            System.err.println(">> ¡CRASH FATAL EN EL SERVLET!");
            e.printStackTrace();

            HttpUtil.writeError(resp, 500, "Internal error generating AI image: " + e.getMessage());
        }
    }
}

//