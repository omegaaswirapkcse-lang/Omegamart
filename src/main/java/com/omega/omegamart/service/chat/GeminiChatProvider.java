package com.omega.omegamart.service.chat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omega.omegamart.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Real AI Chatbot provider integrating with Google Gemini API.
 * Uses HTTP timeouts and safe fallback when credentials or network are unavailable.
 */
public class GeminiChatProvider implements ChatProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiChatProvider.class);
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";
    private static final int TIMEOUT_SECONDS = 6;

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public GeminiChatProvider() {
        this.apiKey = ConfigUtil.get("gemini.apiKey", "");
        this.model = ConfigUtil.get("gemini.model", DEFAULT_MODEL);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    public GeminiChatProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    @Override
    public String generateResponse(String userMessage) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            LOGGER.info("Gemini API key is not configured; falling back to Mock provider.");
            return new MockChatProvider().generateResponse(userMessage);
        }

        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

            // System prompt instructing domain restriction
            String systemPrompt = "You are the AI Assistant for OmegaMart, a multi-vendor e-commerce marketplace. " +
                    "Answer questions politely, concisely, and specifically about products, orders, vendor sales, " +
                    "account management, and shipping. Keep responses under 3 sentences.";

            JsonObject contentObj = new JsonObject();
            JsonArray partsArr = new JsonArray();
            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", systemPrompt + "\nUser Question: " + userMessage);
            partsArr.add(textPart);
            contentObj.add("parts", partsArr);

            JsonArray contentsArr = new JsonArray();
            contentsArr.add(contentObj);

            JsonObject requestJson = new JsonObject();
            requestJson.add("contents", contentsArr);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject resObj = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray candidates = resObj.getAsJsonArray("candidates");
                if (candidates != null && candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    JsonObject candidateContent = firstCandidate.getAsJsonObject("content");
                    JsonArray parts = candidateContent.getAsJsonArray("parts");
                    if (parts != null && parts.size() > 0) {
                        return parts.get(0).getAsJsonObject().get("text").getAsString().trim();
                    }
                }
            } else {
                LOGGER.warn("Gemini API responded with status {}: {}", response.statusCode(), response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Gemini API call interrupted: {}", e.getMessage());
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Gemini API call failed: {}", e.getMessage());
        }

        // Safe fallback answer
        return new MockChatProvider().generateResponse(userMessage);
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }
}
