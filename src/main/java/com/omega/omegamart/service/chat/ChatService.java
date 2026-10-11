package com.omega.omegamart.service.chat;

import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing chatbot interactions, session-level rate limiting, and provider delegation.
 */
public class ChatService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final int MAX_REQUESTS_PER_MINUTE = 10;

    private final ChatProvider chatProvider;
    // Session ID -> Timestamps of recent messages
    private final Map<String, LinkedList<Long>> sessionRateLimits = new ConcurrentHashMap<>();
    // Session ID + Normalized Question -> Answer cache
    private final Map<String, String> responseCache = new ConcurrentHashMap<>();

    public ChatService() {
        String configuredProvider = ConfigUtil.get("chatbot.provider", "MOCK");
        if ("GEMINI".equalsIgnoreCase(configuredProvider)) {
            this.chatProvider = new GeminiChatProvider();
        } else {
            this.chatProvider = new MockChatProvider();
        }
        LOGGER.info("ChatService initialized using [{}] provider.", chatProvider.getProviderName());
    }

    public ChatService(ChatProvider customProvider) {
        this.chatProvider = customProvider;
    }

    /**
     * Processes a chat query from a client session.
     *
     * @param sessionId   HTTP session identifier for rate-limiting
     * @param userMessage raw message text
     * @return AI assistant reply
     */
    public String askQuestion(String sessionId, String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            throw new ValidationException("Message cannot be empty.");
        }

        String trimmed = userMessage.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message exceeds maximum permitted length (" + MAX_MESSAGE_LENGTH + " characters).");
        }

        // Apply Session Rate Limit
        if (sessionId != null) {
            checkRateLimit(sessionId);
        }

        // Check Response Cache
        String cacheKey = (sessionId != null ? sessionId : "global") + "::" + trimmed.toLowerCase();
        if (responseCache.containsKey(cacheKey)) {
            return responseCache.get(cacheKey);
        }

        String reply = chatProvider.generateResponse(trimmed);
        responseCache.put(cacheKey, reply);
        return reply;
    }

    private void checkRateLimit(String sessionId) {
        long now = System.currentTimeMillis();
        long oneMinuteAgo = now - 60_000L;

        LinkedList<Long> timestamps = sessionRateLimits.computeIfAbsent(sessionId, k -> new LinkedList<>());
        synchronized (timestamps) {
            // Remove timestamps older than 1 minute
            while (!timestamps.isEmpty() && timestamps.peekFirst() < oneMinuteAgo) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                LOGGER.warn("Rate limit exceeded for session {}", sessionId);
                throw new ValidationException("You are sending messages too quickly. Please wait a moment before trying again.");
            }

            timestamps.addLast(now);
        }
    }

    public String getActiveProviderName() {
        return chatProvider.getProviderName();
    }
}
