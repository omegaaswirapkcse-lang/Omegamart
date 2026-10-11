# OmegaMart - Requirements Traceability Matrix (REQUIREMENTS.md)

**Capstone Project:** OmegaMart Multi-Vendor E-Commerce Platform  
**Academic Regulation:** Anna University R2025 – Third-Semester CSE  
**Document Purpose:** Maps every mandatory architectural and functional requirement to its implementation code location and verification method.

---

## 1. Non-Negotiable Technology Stack Verification

| Requirement | Prescribed Technology | Implementation Location | Verification Method |
|---|---|---|---|
| Language | Java 17 | `pom.xml` (`<source>17</source>`) | Maven build compiler verification |
| Web Controller | Java Servlets (javax.servlet) | `com.omega.omegamart.controller.*` | Tomcat 9 deployment & unit tests |
| Persistence | JDBC with HikariCP Pool | `com.omega.omegamart.dao.*`, `DatabaseConnectionPool.java` | `DaoIntegrationTest.java` |
| Application Server | Apache Tomcat 9.0.x | `web.xml`, WAR build | Tomcat 9 deployment |
| Build Tool | Apache Maven | `pom.xml` | `mvn clean verify` |
| View Layer | JSP 2.3 + JSTL 1.2 | `src/main/webapp/WEB-INF/views/*.jsp` | Browser rendering, XSS escaping |
| Database | Embedded / Persistent H2 | `schema.sql`, `seed.sql`, `DatabaseInitializer.java` | Schema creation & persistent restart test |
| Testing | JUnit 5 + Mockito + H2 In-Memory | `src/test/java/com/omega/omegamart/*` | JUnit test execution |

---

## 2. Mandatory Features Traceability Matrix

### F1: Registration and Login
- **Specification:** Buyer registration, Seller registration, public admin registration blocked, secure login/logout, session fixation defense, password hashing with BCrypt.
- **Implementation:**
  - Service: [`AuthService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/AuthService.java)
  - Controller: [`AuthController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/AuthController.java)
  - Security Utility: [`PasswordUtil.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/util/PasswordUtil.java)
  - Views: [`login.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/login.jsp), [`register.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/register.jsp)
- **Verification:**
  - Automated Unit Tests: `AuthServiceTest.testRegisterBuyerSuccess()`, `testRegisterDuplicateEmail()`, `testRegisterAdminForbidden()`, `testLoginSuccess()`, `testLoginInvalidPassword()`.

---

### F2: Seller Product Management
- **Specification:** Multi-vendor product creation, updating, deletion, viewing own inventory, server-side field validation, ownership enforcement (seller cannot modify another seller's products).
- **Implementation:**
  - Service: [`ProductService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/ProductService.java) (`createProduct`, `updateProduct`, `deleteProduct`)
  - DAO: [`ProductDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/ProductDao.java) (`findBySellerId`, `updateProduct`, `deleteProduct`)
  - Controller: [`SellerController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/SellerController.java)
  - Views: [`seller-dashboard.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/seller-dashboard.jsp), [`seller-product-form.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/seller-product-form.jsp)
- **Verification:**
  - Automated Unit Tests: `ProductServiceTest.testCreateProductSuccess()`, `testCreateProductInvalidPriceAndStock()`, `testUpdateProductUnauthorized()`, `testDeleteProductUnauthorized()`.

---

### F3: Buyer Product Discovery
- **Specification:** Catalog browse, keyword search, category filtering, pagination, product detail page, stock and availability status.
- **Implementation:**
  - Service: [`ProductService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/ProductService.java) (`getProducts`, `getProductCount`, `getProductById`)
  - DAO: [`ProductDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/ProductDao.java) (`findAll`, `countAll`, `findById`)
  - Controller: [`ProductCatalogController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/ProductCatalogController.java)
  - Views: [`products.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/products.jsp), [`product-detail.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/product-detail.jsp)
- **Verification:**
  - Integration Test: `DaoIntegrationTest.testSeedDataPresence()`
  - Manual E2E: Keyword search combined with category filtering and pagination.

---

### F4: Shopping Cart
- **Specification:** Add products, update quantities, remove items, validate available stock, server-side total recalculation from database values (do not trust client prices).
- **Implementation:**
  - Service: [`CartService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/CartService.java) (`addToCart`, `updateQuantity`, `removeFromCart`, `calculateCartTotal`)
  - DAO: [`CartDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/CartDao.java)
  - Controller: [`CartController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/CartController.java)
  - View: [`cart.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/cart.jsp)
- **Verification:**
  - Automated Unit Tests: `CartServiceTest.testAddToCartSuccess()`, `testAddToCartExceedsStock()`, `testSellerCannotBuyOwnProduct()`, `testCalculateCartTotal()`.
  - Integration Test: `DaoIntegrationTest.testCartWorkflow()`.

---

### F5: Checkout Flow & Transactions
- **Specification:** Multi-vendor transactional checkout, ACID boundaries (`conn.setAutoCommit(false)`, `commit()`, `rollback()`), simulated payment labeling, stock decrements, cart clearing upon order success.
- **Implementation:**
  - Service: [`OrderService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/OrderService.java) (`checkout`)
  - DAO: [`OrderDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/OrderDao.java), [`ProductDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/ProductDao.java) (`updateStock`)
  - Controller: [`CheckoutController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/CheckoutController.java)
  - View: [`checkout.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/checkout.jsp)
- **Verification:**
  - Integration Test: `DaoIntegrationTest.testOrderAndReviewWorkflow()`.
  - Manual E2E: Checkout with simulated payment, verifying inventory decrement in database.

---

### F6: Order Management
- **Specification:** Buyer order history, order receipt details, seller multi-vendor isolation (sellers only see order lines containing their own products).
- **Implementation:**
  - Service: [`OrderService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/OrderService.java) (`getBuyerOrders`, `getSellerOrders`, `getOrderDetails`)
  - DAO: [`OrderDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/OrderDao.java) (`findByBuyerId`, `findBySellerId`, `findItemsByOrderIdAndSellerId`)
  - Controllers: [`OrderController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/OrderController.java), [`SellerController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/SellerController.java)
  - Views: [`orders.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/orders.jsp), [`order-detail.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/order-detail.jsp), [`seller-orders.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/seller-orders.jsp)
- **Verification:**
  - Automated Authorization checks in `OrderService.getOrderDetails()` rejecting cross-user access.

---

### F7: Administration Dashboard
- **Specification:** Protected console for ADMIN role only, platform statistics, user management list, all orders list, product moderation / listing removal.
- **Implementation:**
  - Service: [`ProductService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/ProductService.java) (`adminDeleteProduct`), [`OrderService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/OrderService.java) (`getAllOrdersForAdmin`)
  - Controller: [`AdminController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/AdminController.java)
  - Filter: [`AuthFilter.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/filter/AuthFilter.java)
  - View: [`admin-dashboard.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/admin-dashboard.jsp)
- **Verification:**
  - `AuthFilter` intercepts non-admin requests with HTTP 403 Forbidden.
  - Deactivation moderation updates `is_active = FALSE` in database.

---

### F8: Product Reviews and Star Ratings
- **Specification:** Star ratings (1-5), verified purchase check (only buyers who purchased the product can review), duplicate review prevention, review escaping.
- **Implementation:**
  - Service: [`ReviewService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/ReviewService.java) (`submitReview`, `canUserReviewProduct`)
  - DAO: [`ReviewDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/ReviewDao.java), [`OrderDao.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/dao/OrderDao.java) (`getQualifyingOrderIdForReview`)
  - Controller: [`ReviewController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/ReviewController.java)
  - View: [`product-detail.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/product-detail.jsp)
- **Verification:**
  - Automated Unit Tests: `ReviewServiceTest.testSubmitReviewVerifiedPurchaseSuccess()`, `testSubmitReviewUnverifiedPurchaseRejected()`, `testSubmitDuplicateReviewRejected()`, `testInvalidRatingBounds()`.

---

### Mandatory Final Review Feature: AI Chatbot
- **Specification:** Floating widget, REST endpoint `POST /api/chat`, JSON request/response schema, `ChatProvider` interface, `MockChatProvider` (works without external keys), `GeminiChatProvider` (real AI when configured), per-session rate limit (<= 10 req/min), input validation (<= 300 chars), safe fallback.
- **Implementation:**
  - Interface: [`ChatProvider.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/chat/ChatProvider.java)
  - Providers: [`MockChatProvider.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/chat/MockChatProvider.java), [`GeminiChatProvider.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/chat/GeminiChatProvider.java)
  - Service: [`ChatService.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/service/chat/ChatService.java)
  - Controller: [`ChatApiController.java`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/java/com/omega/omegamart/controller/ChatApiController.java)
  - Frontend: [`footer.jsp`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/WEB-INF/views/common/footer.jsp), [`app.js`](file:///C:/Users/ELCOT/.gemini/antigravity/scratch/omegamart/src/main/webapp/js/app.js)
- **Verification:**
  - Automated Unit Tests: `ChatServiceTest.testFaqOrderPlacement()`, `testFaqSellerRegistration()`, `testFaqPaymentSimulation()`, `testMessageLengthExceeded()`, `testRateLimitingEnforced()`.
