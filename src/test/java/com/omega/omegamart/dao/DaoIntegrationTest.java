package com.omega.omegamart.dao;

import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Category;
import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.OrderItem;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.model.Review;
import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
import com.omega.omegamart.util.DatabaseConnectionPool;
import com.omega.omegamart.util.DatabaseInitializer;
import com.omega.omegamart.util.PasswordUtil;
import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test verifying real JDBC queries, constraints, and transactions against H2 in-memory database.
 */
class DaoIntegrationTest {

    private static UserDao userDao;
    private static CategoryDao categoryDao;
    private static ProductDao productDao;
    private static CartDao cartDao;
    private static OrderDao orderDao;
    private static ReviewDao reviewDao;

    @BeforeAll
    static void initDatabase() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:omegamart_dao_test;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(5);

        DatabaseConnectionPool.initializeWithConfig(config);
        DatabaseInitializer.initialize();

        userDao = new UserDao();
        categoryDao = new CategoryDao();
        productDao = new ProductDao();
        cartDao = new CartDao();
        orderDao = new OrderDao();
        reviewDao = new ReviewDao();
    }

    @AfterAll
    static void tearDown() {
        DatabaseConnectionPool.getInstance().shutdown();
    }

    @Test
    @DisplayName("Integration: Seed data should be populated and accessible")
    void testSeedDataPresence() {
        Optional<User> admin = userDao.findByEmail("admin@omegamart.com");
        assertTrue(admin.isPresent());
        assertEquals(Role.ADMIN, admin.get().getRole());

        List<Category> categories = categoryDao.findAll();
        assertFalse(categories.isEmpty());

        List<Product> products = productDao.findAll(null, null, 0, 10);
        assertFalse(products.isEmpty());
    }

    @Test
    @DisplayName("Integration: Product and Cart workflow")
    void testCartWorkflow() throws SQLException {
        // Create user
        User buyer = new User();
        buyer.setEmail("integ_buyer@example.com");
        buyer.setPasswordHash(PasswordUtil.hashPassword("Pass@123"));
        buyer.setFullName("Integration Buyer");
        buyer.setRole(Role.BUYER);
        buyer = userDao.createUser(buyer);

        // Add product 1 to cart
        cartDao.addToCart(buyer.getId(), 1L, 2);
        List<CartItem> cartItems = cartDao.getCartItems(buyer.getId());
        assertEquals(1, cartItems.size());
        assertEquals(2, cartItems.get(0).getQuantity());

        // Update quantity
        cartDao.updateQuantity(buyer.getId(), 1L, 3);
        cartItems = cartDao.getCartItems(buyer.getId());
        assertEquals(3, cartItems.get(0).getQuantity());

        // Remove from cart
        cartDao.removeFromCart(buyer.getId(), 1L);
        cartItems = cartDao.getCartItems(buyer.getId());
        assertTrue(cartItems.isEmpty());
    }

    @Test
    @DisplayName("Integration: Transactional Order creation and Review submission")
    void testOrderAndReviewWorkflow() throws SQLException {
        User buyer = new User();
        buyer.setEmail("buyer_review_test@example.com");
        buyer.setPasswordHash(PasswordUtil.hashPassword("Pass@123"));
        buyer.setFullName("Review Tester");
        buyer.setRole(Role.BUYER);
        buyer = userDao.createUser(buyer);

        // 1. Transactional order creation
        Order order = new Order();
        order.setOrderNumber("ORD-TEST-999");
        order.setBuyerId(buyer.getId());
        order.setTotalAmount(new BigDecimal("150.00"));
        order.setStatus("DELIVERED");
        order.setShippingAddress("Test Address");
        order.setPaymentMethod("SIMULATED_MOCK_PAYMENT");

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            order = orderDao.createOrder(conn, order);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(1L);
            item.setSellerId(2L);
            item.setProductName("Test Item");
            item.setUnitPrice(new BigDecimal("150.00"));
            item.setQuantity(1);
            item.setSubtotal(new BigDecimal("150.00"));

            orderDao.createOrderItem(conn, item);
            conn.commit();
        }

        // 2. Verify order is retrieved
        Optional<Order> retrievedOrder = orderDao.findById(order.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals("ORD-TEST-999", retrievedOrder.get().getOrderNumber());

        // 3. Check qualifying purchase for review
        Optional<Long> qualifyingOrderId = orderDao.getQualifyingOrderIdForReview(buyer.getId(), 1L);
        assertTrue(qualifyingOrderId.isPresent());

        // 4. Create Review
        Review review = new Review();
        review.setProductId(1L);
        review.setUserId(buyer.getId());
        review.setOrderId(qualifyingOrderId.get());
        review.setRating(5);
        review.setComment("Verified purchase review test.");
        review = reviewDao.createReview(review);

        assertNotNull(review.getId());
        assertTrue(reviewDao.hasUserReviewedProduct(buyer.getId(), 1L));
    }
}
