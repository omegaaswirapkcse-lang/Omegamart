# OmegaMart - Professional Full-Stack E-Commerce Capstone Project

**Academic Regulation:** Anna University R2025 – Third-Semester Computer Science & Engineering (CSE)  
**Project Category:** Capstone Web Application Development  
**System Architecture:** Multi-Vendor E-Commerce Marketplace with Domain-Specific AI Chatbot  

---

## 1. Project Overview & Problem Statement

### Problem Statement
Contemporary commercial e-commerce applications require sophisticated distributed workflows, multi-tenancy access control (buyers, independent merchant sellers, and platform administrators), inventory locking, and transaction consistency. Traditional basic academic web projects often rely on superficial frontend interfaces or monolithic architectures without real database constraints, ACID transaction isolation, secure password handling, or server-side authorization.

### Objectives
**OmegaMart** is designed and implemented as an enterprise-grade full-stack web application adhering to professional software engineering standards:
- **Multi-Vendor Architecture**: Supports distinct user personas—**Buyers** (discovering and purchasing items), **Sellers** (managing inventory and reviewing merchant orders), and **Administrators** (overseeing platform accounts and moderating product listings).
- **Relational Integrity & ACID Transactions**: Real atomic checkouts with inventory stock locks, database-level foreign keys, check constraints, and rollback safeguards.
- **Security Engineering**: Passwords salted and hashed with BCrypt, HikariCP connection pooling, CSRF token defenses, parameterized queries against SQL injection, and server-side Role-Based Access Control (RBAC).
- **Integrated AI Chatbot**: Built-in floating AI assistant with strategy-based provider abstraction (`MockChatProvider` for offline evaluation and `GeminiChatProvider` for live AI) handling marketplace inquiries with rate-limiting and session caching.

---

## 2. Technology Stack

| Layer | Component | Version / Specification | Rationale |
|---|---|---|---|
| **Backend Language** | Java | 17 (LTS) | Modern Java language features, strong typing, and performance. |
| **Web Tier** | Java Servlets | Servlet 4.0 (`javax.servlet`) | Native Tomcat 9 specification without high-overhead frameworks. |
| **Persistence** | JDBC + HikariCP | 5.1.0 | High-performance enterprise connection pooling; zero ORM abstraction leak. |
| **Database** | Embedded / Persistent H2 | 2.2.224 | Zero external service setup; persistent file storage across restarts + in-memory for testing. |
| **Application Server** | Apache Tomcat | 9.0.x | Industry standard servlet container matching Anna University syllabus. |
| **Build Management** | Apache Maven | 3.8+ | Standardized dependency management, automated testing, and WAR packaging. |
| **View Tier** | JSP 2.3 + JSTL 1.2 | Standard Tag Library | Server-side HTML rendering with strict XSS escaping. |
| **Styling & Client** | HTML5, CSS3, JavaScript | Modern CSS Variables | Responsive, lightweight design system without heavy JS bundle dependencies. |
| **Security** | jBCrypt | 0.4 | Adaptive work factor 10 password salting. |
| **Testing** | JUnit 5 + Mockito | 5.10.2 / 5.11.0 | Unit tests for services and integration tests with H2 in-memory DB. |
| **Quality & CI** | Checkstyle, SpotBugs, GitHub Actions | Latest | Automated static code analysis and continuous integration pipeline. |

---

## 3. Predefined Demonstration Credentials

For evaluation and viva demonstrations, OmegaMart automatically seeds and synchronizes the following accounts on startup:

| Role | Email Address | Password | Capabilities |
|---|---|---|---|
| **👑 System Admin** | `admin@omegamart.com` | `Admin@12345` | Global oversight, user directory, product moderation, all orders |
| **🏬 Demo Seller 1** | `seller1@omegamart.com` | `Seller@12345` | Tech vendor: list products, edit stock, fulfill customer orders |
| **🏬 Demo Seller 2** | `seller2@omegamart.com` | `Seller@12345` | Fashion boutique: manage catalog, view incoming orders |
| **🛒 Demo Buyer 1** | `buyer1@omegamart.com` | `Buyer@12345` | Pre-seeded with a completed order to demonstrate **Verified Reviews** |
| **🛒 Demo Buyer 2** | `buyer2@omegamart.com` | `Buyer@12345` | Fresh buyer account for testing browse-to-checkout flows |

*Note: The login page includes one-click "Quick-Fill" buttons for Admin, Seller, and Buyer to facilitate fast and convenient live demonstrations during project reviews.*

---

## 4. Key Features & Implementation Highlights

### F1: Registration and Authentication
- **Role Isolation**: Public registration enables Buyer and Seller accounts; public Admin registration is strictly blocked.
- **Session Fixation Defense**: Sessions are invalidated and regenerated upon successful authentication (`request.getSession(true)` with fresh IDs).
- **Session Timeout**: Explicit 30-minute session expiration enforced.

### F2: Seller Product Management & Ownership
- Sellers can create, view, edit, and delete their products.
- **Server-Side Ownership Enforcement**: `ProductService` verifies `product.sellerId.equals(currentUserId)`. Any attempt by an unauthorized seller to manipulate another vendor's product ID via URL or form is rejected with HTTP 403 Forbidden.

### F3: Buyer Product Discovery
- Keyword search querying name and description.
- Category filtering with combined query parameters.
- Pagination controls with calculated total pages.
- Real-time stock display and rating summaries.

### F4: Shopping Cart & Server-Side Integrity
- Add, increment, decrement, and remove items.
- **Never Trust Client Totals**: Subtotals and totals are recalculated server-side using current database prices.
- Prevents sellers from purchasing their own listed products.

### F5: Transactional Checkout (ACID)
- Transaction boundary initiated with `conn.setAutoCommit(false)`.
- Stock quantities re-verified against live database values.
- Atomic stock decrements prevent concurrent overselling.
- Cart is cleared only upon successful transaction commit; rolls back automatically on error.
- Mock payment simulation with prominent demonstration disclosure.

### F6: Multi-Vendor Order Management
- Buyers can view their order history and receipts.
- Sellers can view only the order items containing products they sell, preserving multi-tenant isolation.

### F7: Administration & Moderation
- Dashboard with total user counts, active products, and global transactions.
- Product moderation enables administrative deactivation of inappropriate listings.

### F8: Verified Purchase Reviews & Star Ratings
- Star ratings from 1 to 5 with feedback text.
- **Verified Purchase Enforcement**: The backend verifies `orderDao.getQualifyingOrderIdForReview(buyerId, productId)`. Buyers who have not purchased the item are blocked from submitting reviews.
- Duplicate review policy prevents spamming.

### AI Chatbot (Final Review Mandatory Deliverable)
- Strategy Pattern: `ChatProvider` interface with `MockChatProvider` and `GeminiChatProvider`.
- Floating widget accessible across all pages via `chatWidgetBtn`.
- REST API endpoint: `POST /api/chat`.
- Sliding window rate limiting: maximum 10 requests per minute per session.
- Response caching to avoid redundant calculations.

---

## 5. Local Setup & Execution Guide

### Prerequisites
1. **Java Development Kit (JDK) 17**: Ensure `java -version` reports 17.x.
2. **Apache Maven 3.8+**: Ensure `mvn -version` is accessible.
3. **Apache Tomcat 9.0.x** (optional for standalone WAR deployment).

### Build & Automated Tests
To compile the project and execute all unit and integration tests:
```bash
mvn clean test
```

To run code quality audits (Checkstyle and SpotBugs):
```bash
mvn checkstyle:check
mvn spotbugs:check
```

To package the production WAR file:
```bash
mvn clean package
```
The resulting deployable WAR will be generated at: `target/omegamart.war`.

### Running with Apache Tomcat 9
1. Copy `target/omegamart.war` to your Tomcat installation's `webapps/` folder:
   ```bash
   copy target\omegamart.war "C:\path\to\apache-tomcat-9.0.x\webapps\"
   ```
2. Start Tomcat:
   ```bash
   "C:\path\to\apache-tomcat-9.0.x\bin\startup.bat"
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8080/omegamart/
   ```

---

## 6. Project Directory Structure

```
omegamart/
├── .github/workflows/ci.yml         # GitHub Actions CI pipeline
├── .env.example                     # Environment configuration template
├── .gitignore                       # Git ignore rules
├── checkstyle.xml                   # Code quality rules
├── pom.xml                          # Maven build configuration
├── REQUIREMENTS.md                  # Requirements traceability matrix
├── TASK_TRACKER.md                  # Implementation status board
├── ARCHITECTURE.md                  # Layered architecture & Mermaid diagrams
├── README.md                        # Project documentation & viva guide
├── src/
│   ├── main/
│   │   ├── java/com/omega/omegamart/
│   │   │   ├── controller/          # Servlets handling HTTP requests
│   │   │   ├── dao/                 # JDBC Data Access Objects
│   │   │   ├── exception/           # Custom domain exceptions
│   │   │   ├── filter/              # AuthFilter & CsrfFilter
│   │   │   ├── listener/            # AppContextListener (lifecycle)
│   │   │   ├── model/               # Domain entity models (POJOs)
│   │   │   ├── service/             # Business rules & transactions
│   │   │   │   └── chat/            # Chatbot provider implementations
│   │   │   └── util/                # DB pool, BCrypt, Config utilities
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── db/
│   │   │       ├── schema.sql       # Relational DDL script
│   │   │       └── seed.sql         # Demo demonstration data
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   ├── web.xml          # Tomcat deployment descriptor
│   │       │   └── views/           # JSP server-rendered templates
│   │       ├── css/style.css        # Responsive design system
│   │       ├── js/app.js            # Chatbot and client interactions
│   │       └── index.jsp            # Root landing forwarder
│   └── test/java/com/omega/omegamart/
│       ├── dao/                     # H2 In-Memory Integration tests
│       └── service/                 # JUnit 5 & Mockito service unit tests
```

---

## 7. Viva Voce & Technical Defense Q&A

### Q1: Why did you choose Java Servlets and JDBC over Spring Boot?
**Answer:** In accordance with the Anna University R2025 capstone curriculum, Servlets and JDBC demonstrate fundamental understanding of the Java web ecosystem, HTTP request-response lifecycles, and database connection management without relying on framework magic or auto-configuration abstractions.

### Q2: How does the application maintain database consistency during checkout?
**Answer:** The checkout method in `OrderService` initiates an explicit database transaction by disabling auto-commit (`conn.setAutoCommit(false)`). It verifies current stock, decrements inventory, creates the master order and individual order items, and empties the cart within the same connection. If any step fails or insufficient stock is detected, `conn.rollback()` restores the database to its exact prior state.

### Q3: How do you prevent a seller from modifying another seller's products?
**Answer:** We enforce ownership verification at the service layer in `ProductService.updateProduct` and `deleteProduct`. The method compares the logged-in seller's ID against `existingProduct.getSellerId()`. If they do not match, an `UnauthorizedException` is thrown, resulting in an HTTP 403 Forbidden response.

### Q4: Why is PreparedStatement used instead of Statement?
**Answer:** `PreparedStatement` pre-compiles SQL queries on the database and treats user inputs as parameterized values rather than executable SQL code, completely neutralizing SQL Injection attacks. Additionally, it offers performance advantages via database statement caching managed by HikariCP.

### Q5: How is the AI chatbot structured to support multiple providers?
**Answer:** We implemented the **Strategy Pattern** through the `ChatProvider` interface. The `ChatService` can delegate queries either to `MockChatProvider` (which performs intelligent pattern matching against common marketplace FAQs offline) or `GeminiChatProvider` (which calls Google's Generative AI REST API). This allows seamless operation during academic evaluation even without internet access or API credentials.
