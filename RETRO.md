# OmegaMart Project Retrospective (RETRO.md)

## What Went Well
- **Layered Architecture**: The clear separation between Controller, Service, DAO, and Model made development modular and easy to reason about.
- **Security by Design**: BCrypt hashing, parameterized queries, and role-based filtering ensure enterprise-grade protection.
- **Embedded Database Choice**: Using H2 in persistent file mode for runtime and in-memory for testing eliminated the need for external database servers during local development while maintaining ACID guarantees.
- **AI Chatbot Strategy Pattern**: The `ChatProvider` abstraction allows seamless offline demonstration using `MockChatProvider` with zero dependency on external network access or paid API keys.

## Challenges & Solutions
1. **Multi-Vendor Order Isolation**:
   - *Challenge*: Sellers should only see items they own within multi-item orders placed by buyers.
   - *Solution*: Designed `findItemsByOrderIdAndSellerId()` in `OrderDao` and filtered order item views dynamically.
2. **Review Integrity**:
   - *Challenge*: Preventing unverified or duplicate reviews.
   - *Solution*: Added a query checking for completed customer orders containing the product before allowing review submission.

## Future Improvements
- Implement email notifications using JavaMail API.
- Add image upload with secure server-side file format inspection.
- Introduce advanced full-text search with Lucene.
