package com.example.demo.service;

import com.example.demo.util.AppConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAIService {

    private static final String OPENAI_URL = "https://api.openai.com/v1/images/generations";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public String generarImagen(String prompt) throws Exception {
        String apiKey = AppConfig.openaiApiKey();

        JsonObject body = new JsonObject();
        body.addProperty("model", "dall-e-3");
        body.addProperty("prompt", prompt);
        body.addProperty("n", 1);
        body.addProperty("size", "1024x1024");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            JsonObject err = GSON.fromJson(response.body(), JsonObject.class);
            String msg = err.has("error")
                    ? err.getAsJsonObject("error").get("message").getAsString()
                    : response.body();
            throw new RuntimeException(msg);
        }

        return GSON.fromJson(response.body(), JsonObject.class)
                .getAsJsonArray("data")
                .get(0).getAsJsonObject()
                .get("url").getAsString();
    }
}
