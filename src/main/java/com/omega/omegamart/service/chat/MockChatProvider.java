package com.omega.omegamart.service.chat;

import java.util.Locale;

/**
 * Robust mock implementation providing intelligent keyword-matched FAQ answers
 * for OmegaMart marketplace inquiries without requiring an external API key or network access.
 */
public class MockChatProvider implements ChatProvider {

    @Override
    public String generateResponse(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! How can I assist you with your OmegaMart shopping experience today?";
        }

        String msg = userMessage.toLowerCase(Locale.ROOT).trim();

        if (msg.contains("place an order") || msg.contains("how to order") || msg.contains("how do i buy") || msg.contains("checkout")) {
            return "To place an order: Browse our product catalog, click 'Add to Cart' on your desired item, navigate to your Shopping Cart, and proceed to Checkout. Enter your shipping address and confirm your simulated payment.";
        }

        if (msg.contains("seller") || msg.contains("how to sell") || msg.contains("list product") || msg.contains("create product")) {
            return "To sell on OmegaMart: Register an account with the 'Seller' role. Once logged in, visit your Seller Dashboard to add, update, or remove your product listings, set inventory stock levels, and review incoming orders.";
        }

        if (msg.contains("payment") || msg.contains("pay") || msg.contains("credit card")) {
            return "OmegaMart currently supports a simulated mock payment confirmation for demonstration and academic evaluation purposes. No real monetary transactions are charged.";
        }

        if (msg.contains("review") || msg.contains("rating") || msg.contains("stars")) {
            return "Product reviews and 1-5 star ratings can be submitted by verified buyers who have purchased and received the product. This ensures all customer feedback is authentic and trustworthy.";
        }

        if (msg.contains("shipping") || msg.contains("delivery") || msg.contains("track")) {
            return "Standard delivery is simulated within 3 to 5 business days across India. You can view your order status and shipping details under the 'My Orders' section in your account.";
        }

        if (msg.contains("cancel") || msg.contains("return") || msg.contains("refund")) {
            return "Orders can be reviewed under 'My Orders'. For simulated orders, sellers manage processing and fulfillment through their vendor dashboard.";
        }

        if (msg.contains("security") || msg.contains("password") || msg.contains("safe")) {
            return "OmegaMart enforces enterprise-grade security: passwords are salted and hashed using BCrypt, database connections use HikariCP and parameterized SQL statements to prevent injection, and sensitive routes are protected with role-based authorization filters.";
        }

        if (msg.contains("admin") || msg.contains("moderate") || msg.contains("administrator")) {
            return "System Administrators oversee the platform through a dedicated dashboard to moderate listings, monitor registered buyers and sellers, and review overall marketplace order volume.";
        }

        if (msg.contains("search") || msg.contains("category") || msg.contains("filter")) {
            return "You can discover products by typing keywords in the search bar or filtering by category (Electronics, Fashion, Home & Living, Books, Sports) directly on our home and catalog pages.";
        }

        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "Welcome to OmegaMart! I am your AI assistant. You can ask me about placing orders, selling items, simulated payments, reviews, and platform policies.";
        }

        return "I can assist you with marketplace questions such as placing orders, selling products, reviews, simulated payments, and category search. Please ask any question about OmegaMart!";
    }

    @Override
    public String getProviderName() {
        return "MOCK";
    }
}
