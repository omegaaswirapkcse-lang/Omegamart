package com.omega.omegamart.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.service.chat.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * RESTful API Controller handling domain-specific AI chatbot queries.
 */
@WebServlet(name = "ChatApiController", urlPatterns = {"/api/chat"})
public class ChatApiController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatApiController.class);
    private final ChatService chatService = new ChatService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(true);
        String sessionId = session.getId();

        JsonObject responseObj = new JsonObject();

        try {
            // Read JSON body
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String body = sb.toString();
            if (body.trim().isEmpty()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Empty request body.");
                return;
            }

            JsonObject requestJson;
            try {
                requestJson = JsonParser.parseString(body).getAsJsonObject();
            } catch (Exception e) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON request.");
                return;
            }

            if (!requestJson.has("message") || requestJson.get("message").isJsonNull()) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing 'message' field.");
                return;
            }

            String userMessage = requestJson.get("message").getAsString();
            String reply = chatService.askQuestion(sessionId, userMessage);

            JsonObject dataObj = new JsonObject();
            dataObj.addProperty("reply", reply);
            dataObj.addProperty("provider", chatService.getActiveProviderName());

            responseObj.addProperty("success", true);
            responseObj.add("data", dataObj);
            responseObj.add("error", null);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(responseObj));

        } catch (ValidationException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Chatbot processing error: {}", e.getMessage(), e);
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred. Please try again.");
        }
    }

    private void sendErrorResponse(HttpServletResponse resp, int statusCode, String errorMessage) throws IOException {
        resp.setStatus(statusCode);
        JsonObject errorObj = new JsonObject();
        errorObj.addProperty("success", false);
        errorObj.add("data", null);
        errorObj.addProperty("error", errorMessage);
        resp.getWriter().write(gson.toJson(errorObj));
    }
}
