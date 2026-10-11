<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

</div> <!-- End of .main-content -->

<footer class="site-footer">
    <div class="footer-container">
        <div>
            <h4 style="color: white; margin-bottom: 0.75rem; font-size: 1.1rem;">OmegaMart Marketplace</h4>
            <p style="margin-bottom: 0.5rem;">
                Anna University R2025 Third-Semester CSE Capstone Project.
                A production-grade multi-vendor e-commerce architecture engineered with Java Servlets, JDBC, and Apache Tomcat 9.
            </p>
        </div>
        <div>
            <h4 style="color: white; margin-bottom: 0.75rem; font-size: 0.95rem;">Quick Links</h4>
            <ul style="list-style: none; display: flex; flex-direction: column; gap: 0.4rem;">
                <li><a href="${pageContext.request.contextPath}/products" style="color: #94a3b8; text-decoration: none;">Browse Catalog</a></li>
                <li><a href="${pageContext.request.contextPath}/register" style="color: #94a3b8; text-decoration: none;">Become a Seller</a></li>
                <li><a href="${pageContext.request.contextPath}/login" style="color: #94a3b8; text-decoration: none;">Sign In</a></li>
            </ul>
        </div>
        <div>
            <h4 style="color: white; margin-bottom: 0.75rem; font-size: 0.95rem;">Platform Security</h4>
            <p style="font-size: 0.82rem;">
                • BCrypt Salted Passwords<br>
                • HikariCP Connection Pooling<br>
                • Server-Side Role-Based Authorization<br>
                • ACID Checkout Transactions
            </p>
        </div>
    </div>
    <div class="footer-bottom">
        &copy; 2026 OmegaMart. Designed and Developed for Anna University R2025 Capstone Evaluation.
    </div>
</footer>

<!-- FLOATING AI CHATBOT WIDGET -->
<button id="chatWidgetBtn" class="chat-widget-button" aria-label="Open AI Assistant" title="Chat with OmegaMart AI">
    💬
</button>

<div id="chatModal" class="chat-modal" role="dialog" aria-labelledby="chatTitle">
    <div class="chat-header">
        <h3 id="chatTitle">🤖 OmegaMart AI Assistant</h3>
        <button id="chatCloseBtn" class="chat-close-btn" aria-label="Close Chat">✕</button>
    </div>
    <div id="chatMessages" class="chat-messages">
        <div class="chat-bubble chat-bubble-bot">
            Hello! I am your OmegaMart Assistant. How can I help you today? You can ask about ordering, selling, payments, reviews, or platform features!
        </div>
    </div>
    <form id="chatForm" class="chat-input-area">
        <input type="text" id="chatInput" placeholder="Ask a question..." autocomplete="off" maxlength="300" required/>
        <button type="submit">Send</button>
    </form>
</div>

<script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
