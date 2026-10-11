/**
 * OmegaMart Frontend Application Scripts
 * Manages the floating AI Chatbot widget, demo logins, and UI interactions.
 */

document.addEventListener("DOMContentLoaded", function () {
    initChatbot();
    initDemoLoginHelpers();
});

/**
 * Initializes the AI Chatbot floating widget
 */
function initChatbot() {
    const chatBtn = document.getElementById("chatWidgetBtn");
    const chatModal = document.getElementById("chatModal");
    const chatClose = document.getElementById("chatCloseBtn");
    const chatForm = document.getElementById("chatForm");
    const chatInput = document.getElementById("chatInput");
    const chatMessages = document.getElementById("chatMessages");

    if (!chatBtn || !chatModal) return;

    chatBtn.addEventListener("click", function () {
        const isHidden = chatModal.style.display === "none" || !chatModal.style.display;
        chatModal.style.display = isHidden ? "flex" : "none";
        if (isHidden && chatInput) {
            chatInput.focus();
        }
    });

    if (chatClose) {
        chatClose.addEventListener("click", function () {
            chatModal.style.display = "none";
        });
    }

    if (chatForm && chatInput && chatMessages) {
        chatForm.addEventListener("submit", function (e) {
            e.preventDefault();
            const message = chatInput.value.trim();
            if (!message) return;

            // Render user bubble
            appendChatBubble(chatMessages, message, "chat-bubble-user");
            chatInput.value = "";

            // Render temporary typing indicator
            const loadingBubble = appendChatBubble(chatMessages, "Thinking...", "chat-bubble-bot");

            // Make asynchronous API call to /api/chat
            fetch(window.location.origin + getContextPath() + "/api/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ message: message })
            })
            .then(function (res) {
                return res.json();
            })
            .then(function (data) {
                loadingBubble.remove();
                if (data.success && data.data && data.data.reply) {
                    appendChatBubble(chatMessages, data.data.reply, "chat-bubble-bot");
                } else {
                    const errMsg = data.error || "Sorry, I couldn't process your request right now.";
                    appendChatBubble(chatMessages, errMsg, "chat-bubble-bot");
                }
            })
            .catch(function (err) {
                loadingBubble.remove();
                appendChatBubble(chatMessages, "Network error. Please try again later.", "chat-bubble-bot");
            });
        });
    }
}

function appendChatBubble(container, text, className) {
    const bubble = document.createElement("div");
    bubble.className = "chat-bubble " + className;
    bubble.textContent = text;
    container.appendChild(bubble);
    container.scrollTop = container.scrollHeight;
    return bubble;
}

function getContextPath() {
    const pathName = window.location.pathname;
    const firstSlash = pathName.indexOf("/", 1);
    // If running in Tomcat with context root e.g. /omegamart
    if (firstSlash !== -1 && pathName.startsWith("/omegamart")) {
        return "/omegamart";
    }
    return "";
}

/**
 * Quick-fill login buttons for easy viva and evaluation demonstration
 */
function initDemoLoginHelpers() {
    window.fillCredentials = function (email, password) {
        const emailInput = document.getElementById("email");
        const passwordInput = document.getElementById("password");
        if (emailInput && passwordInput) {
            emailInput.value = email;
            passwordInput.value = password;
        }
    };
}
