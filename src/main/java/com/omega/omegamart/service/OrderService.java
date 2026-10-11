package com.omega.omegamart.service;

import com.omega.omegamart.dao.CartDao;
import com.omega.omegamart.dao.OrderDao;
import com.omega.omegamart.dao.ProductDao;
import com.omega.omegamart.exception.ResourceNotFoundException;
import com.omega.omegamart.exception.UnauthorizedException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.OrderItem;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.model.Role;
import com.omega.omegamart.util.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Service managing order checkout transactions and multi-vendor order visibility.
 */
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final CartDao cartDao;

    public OrderService() {
        this(new OrderDao(), new ProductDao(), new CartDao());
    }

    public OrderService(OrderDao orderDao, ProductDao productDao, CartDao cartDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.cartDao = cartDao;
    }

    /**
     * Executes order checkout within an ACID database transaction.
     * Prevents overselling, recalculates prices from database, updates stock,
     * persists order items, and empties cart upon success.
     */
    public Order checkout(Long buyerId, String shippingAddress) {
        if (shippingAddress == null || shippingAddress.trim().isEmpty() || shippingAddress.trim().length() > 500) {
            throw new ValidationException("A valid shipping delivery address is required (max 500 characters).");
        }

        List<CartItem> cartItems = cartDao.getCartItems(buyerId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ValidationException("Your shopping cart is empty.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnectionPool.getInstance().getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            BigDecimal orderTotal = BigDecimal.ZERO;

            // 1. Validate stock and recalculate totals with live database values
            for (CartItem item : cartItems) {
                Product liveProduct = productDao.findById(item.getProductId())
                        .orElseThrow(() -> new ValidationException("Product " + item.getProductId() + " is no longer available."));

                if (!Boolean.TRUE.equals(liveProduct.getIsActive())) {
                    throw new ValidationException("Product '" + liveProduct.getName() + "' is currently inactive.");
                }

                if (liveProduct.getStockQuantity() < item.getQuantity()) {
                    throw new ValidationException("Insufficient stock for product '" + liveProduct.getName() +
                            "'. Available: " + liveProduct.getStockQuantity() + ", in cart: " + item.getQuantity());
                }

                BigDecimal itemSubtotal = liveProduct.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                orderTotal = orderTotal.add(itemSubtotal);
            }

            // 2. Create the master Order record
            String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            Order order = new Order();
            order.setOrderNumber(orderNumber);
            order.setBuyerId(buyerId);
            order.setTotalAmount(orderTotal);
            order.setStatus("CONFIRMED");
            order.setShippingAddress(shippingAddress.trim());
            order.setPaymentMethod("SIMULATED_MOCK_PAYMENT");

            order = orderDao.createOrder(conn, order);

            // 3. Create individual Order Items and decrement inventory stock
            for (CartItem item : cartItems) {
                Product liveProduct = productDao.findById(item.getProductId()).get();

                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(liveProduct.getId());
                orderItem.setSellerId(liveProduct.getSellerId());
                orderItem.setProductName(liveProduct.getName()); // Historical record preserved
                orderItem.setUnitPrice(liveProduct.getPrice());   // Historical purchase price preserved
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSubtotal(liveProduct.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                orderDao.createOrderItem(conn, orderItem);

                // Decrement stock
                int remainingStock = liveProduct.getStockQuantity() - item.getQuantity();
                boolean stockUpdated = productDao.updateStock(conn, liveProduct.getId(), remainingStock);
                if (!stockUpdated) {
                    throw new SQLException("Failed to update stock quantity for product: " + liveProduct.getId());
                }
            }

            // 4. Clear the buyer's cart
            cartDao.clearCart(conn, buyerId);

            // 5. Commit transaction
            conn.commit();
            LOGGER.info("Order {} successfully created for buyer {} total {}", order.getOrderNumber(), buyerId, orderTotal);

            // Load populated order items for confirmation display
            order.setItems(orderDao.findItemsByOrderId(order.getId()));
            return order;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    LOGGER.warn("Checkout transaction failed; rolling back changes: {}", e.getMessage());
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.error("Rollback failed: {}", rollbackEx.getMessage(), rollbackEx);
                }
            }
            if (e instanceof ValidationException) {
                throw (ValidationException) e;
            }
            throw new RuntimeException("Checkout failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    LOGGER.error("Failed to close connection: {}", closeEx.getMessage(), closeEx);
                }
            }
        }
    }

    public Order getOrderDetails(Long orderId, Long currentUserId, Role currentUserRole) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order with ID " + orderId + " not found."));

        // Role-based authorization:
        // Admin: can view all orders
        // Buyer: can only view their own orders
        // Seller: can only view orders containing their products
        if (currentUserRole == Role.ADMIN) {
            return order;
        }

        if (currentUserRole == Role.BUYER) {
            if (!order.getBuyerId().equals(currentUserId)) {
                LOGGER.warn("Buyer {} attempted to view unauthorized order {}", currentUserId, orderId);
                throw new UnauthorizedException("You are not authorized to view this order.");
            }
            return order;
        }

        if (currentUserRole == Role.SELLER) {
            List<OrderItem> sellerItems = orderDao.findItemsByOrderIdAndSellerId(orderId, currentUserId);
            if (sellerItems.isEmpty()) {
                LOGGER.warn("Seller {} attempted to view unauthorized order {}", currentUserId, orderId);
                throw new UnauthorizedException("You are not authorized to view this order.");
            }
            order.setItems(sellerItems);
            return order;
        }

        throw new UnauthorizedException("Unauthorized access to order.");
    }

    public List<Order> getBuyerOrders(Long buyerId) {
        return orderDao.findByBuyerId(buyerId);
    }

    public List<Order> getSellerOrders(Long sellerId) {
        return orderDao.findBySellerId(sellerId);
    }

    public List<Order> getAllOrdersForAdmin() {
        return orderDao.findAllOrders();
    }
}
