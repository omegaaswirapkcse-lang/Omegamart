package com.omega.omegamart.service.chat;

/**
 * Strategy interface for marketplace AI chatbot providers.
 * Decouples the chatbot controller and service from specific AI implementations.
 */
public interface ChatProvider {

    /**
     * Generates an answer to a user's marketplace inquiry.
     *
     * @param userMessage user's input question
     * @return contextual marketplace answer
     */
    String generateResponse(String userMessage);

    /**
     * Returns the name of the provider implementation.
     */
    String getProviderName();
}
