package com.omega.omegamart.service.chat;

import com.omega.omegamart.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(new MockChatProvider());
    }

    @Test
    @DisplayName("Should answer FAQ regarding placing an order")
    void testFaqOrderPlacement() {
        String reply = chatService.askQuestion("session-1", "How do I place an order?");
        assertNotNull(reply);
        assertTrue(reply.contains("Browse our product catalog") || reply.contains("Checkout"));
    }

    @Test
    @DisplayName("Should answer FAQ regarding vendor product selling")
    void testFaqSellerRegistration() {
        String reply = chatService.askQuestion("session-1", "How can I become a seller?");
        assertNotNull(reply);
        assertTrue(reply.contains("Seller") || reply.contains("dashboard"));
    }

    @Test
    @DisplayName("Should answer FAQ regarding mock simulated payment")
    void testFaqPaymentSimulation() {
        String reply = chatService.askQuestion("session-1", "What payment method do you accept?");
        assertNotNull(reply);
        assertTrue(reply.contains("mock payment") || reply.contains("simulated"));
    }

    @Test
    @DisplayName("Should reject messages exceeding maximum character limit")
    void testMessageLengthExceeded() {
        String longMessage = "a".repeat(350);
        ValidationException ex = assertThrows(ValidationException.class, () ->
                chatService.askQuestion("session-1", longMessage)
        );
        assertTrue(ex.getMessage().contains("exceeds maximum permitted length"));
    }

    @Test
    @DisplayName("Should enforce session rate limiting after 10 requests per minute")
    void testRateLimitingEnforced() {
        String sessionId = "rate-limit-session";
        // Send 10 allowed messages
        for (int i = 0; i < 10; i++) {
            assertNotNull(chatService.askQuestion(sessionId, "Question " + i));
        }

        // 11th message should be blocked by rate limiter
        ValidationException ex = assertThrows(ValidationException.class, () ->
                chatService.askQuestion(sessionId, "Question 11")
        );
        assertTrue(ex.getMessage().contains("sending messages too quickly"));
    }
}
