# OmegaMart Implementation Task Tracker

**Status Key:**
- `Not started`
- `In progress`
- `Implemented`
- `Tested`
- `Verified`

---

## Task Progress Board

| Task ID | Component / Milestone | Primary Files | Status | Notes & Verification Method |
|---|---|---|---|---|
| **ENV-01** | Project Initialization & Maven Config | `pom.xml`, `.gitignore`, `.env.example`, `checkstyle.xml` | **Verified** | Standard Maven webapp structure established for Tomcat 9 & Java 17 |
| **DB-01** | Relational Database Schema Design | `schema.sql`, `seed.sql` | **Verified** | ANSI SQL DDL with ANSI types, foreign keys, and indexes |
| **DB-02** | HikariCP Connection Pool & Lifecycle | `DatabaseConnectionPool.java`, `AppContextListener.java` | **Verified** | Singleton pool initialized in listener; persistent H2 file mode |
| **DB-03** | Automated DB Initializer & Migrations | `DatabaseInitializer.java` | **Verified** | Idempotent schema loader & demo account synchronizer |
| **SEC-01** | Password Hashing & Salts | `PasswordUtil.java` | **Verified** | jBCrypt with adaptive work factor 10 |
| **SEC-02** | Role-Based Access Control (RBAC) Filter | `AuthFilter.java` | **Verified** | Protects `/buyer/*`, `/seller/*`, `/admin/*` routes |
| **SEC-03** | Cross-Site Request Forgery (CSRF) Guard | `CsrfFilter.java` | **Verified** | Cryptographic token generation & validation on state-modifying requests |
| **F1-01** | Authentication & Registration Service | `AuthService.java`, `UserDao.java`, `AuthController.java` | **Verified** | Tested via `AuthServiceTest` (5 unit tests) |
| **F1-02** | User Interface: Login & Register | `login.jsp`, `register.jsp` | **Verified** | Demo credentials quick-fill buttons included for evaluation |
| **F2-01** | Multi-Vendor Seller Product Management | `ProductService.java`, `ProductDao.java`, `SellerController.java` | **Verified** | Tested via `ProductServiceTest` (strict ownership enforcement) |
| **F2-02** | Seller Dashboard & Product Forms | `seller-dashboard.jsp`, `seller-product-form.jsp` | **Verified** | Responsive table, inventory levels, category dropdown |
| **F3-01** | Buyer Product Discovery & Search | `ProductService.java`, `ProductCatalogController.java` | **Verified** | Tested via `DaoIntegrationTest` (keyword + category filter + pagination) |
| **F3-02** | Storefront & Detail Views | `index.jsp`, `products.jsp`, `product-detail.jsp` | **Verified** | Responsive product cards, category chips, rating badges |
| **F4-01** | Shopping Cart Logic & Server Totals | `CartService.java`, `CartDao.java`, `CartController.java` | **Verified** | Tested via `CartServiceTest` & `DaoIntegrationTest` |
| **F4-02** | Shopping Cart UI | `cart.jsp` | **Verified** | Quantity adjustment, removal, server total calculation |
| **F5-01** | Transactional Checkout Flow (ACID) | `OrderService.java`, `OrderDao.java`, `CheckoutController.java` | **Verified** | Transactional commit/rollback, stock decrements, overselling prevention |
| **F5-02** | Simulated Mock Payment UI | `checkout.jsp` | **Verified** | Clear simulated payment disclosure, address input, price review |
| **F6-01** | Order History & Multi-Vendor Visibility | `OrderService.java`, `OrderController.java`, `SellerController.java` | **Verified** | Buyer sees own orders; Sellers see only their product items |
| **F6-02** | Order Receipts & Fulfillment Views | `orders.jsp`, `order-detail.jsp`, `seller-orders.jsp` | **Verified** | Line items, unit prices, date/time, delivery address |
| **F7-01** | Administrator Dashboard & Governance | `AdminController.java`, `admin-dashboard.jsp` | **Verified** | Metrics overview, user directory, product moderation |
| **F8-01** | Product Reviews & Star Ratings | `ReviewService.java`, `ReviewDao.java`, `ReviewController.java` | **Verified** | Tested via `ReviewServiceTest` (verified purchase enforcement) |
| **F8-02** | Review Presentation & Submission UI | `product-detail.jsp` | **Verified** | Star display, conditional submission form for verified buyers |
| **AI-01** | AI Chatbot Service & Providers | `ChatProvider.java`, `MockChatProvider.java`, `GeminiChatProvider.java` | **Verified** | Tested via `ChatServiceTest` (FAQ matching, length, rate limit) |
| **AI-02** | Chat REST API Endpoint | `ChatApiController.java` (`POST /api/chat`) | **Verified** | JSON input/output schema, session rate-limiting |
| **AI-03** | Floating Chat Widget UI | `style.css`, `app.js`, `footer.jsp` | **Verified** | Floating bubble button, chat modal, responsive mobile support |
| **QA-01** | Unit & Integration Test Suite | `AuthServiceTest`, `ProductServiceTest`, `CartServiceTest`, etc. | **Verified** | JUnit 5 + Mockito + H2 In-Memory DAO tests |
| **DOC-01** | Architectural & Requirements Documentation | `REQUIREMENTS.md`, `TASK_TRACKER.md`, `ARCHITECTURE.md` | **Verified** | Diagrams in Mermaid (ER, Use Case, Sequence) |
| **DOC-02** | User & Evaluator Guide | `README.md` | **Verified** | Complete setup, architecture, and viva defense guide |
| **CI-01** | GitHub Actions Workflow | `.github/workflows/ci.yml` | **Verified** | Java 17, Maven test, Checkstyle, SpotBugs pipeline |
