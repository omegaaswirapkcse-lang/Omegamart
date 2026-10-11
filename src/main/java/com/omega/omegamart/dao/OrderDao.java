package com.omega.omegamart.dao;

import com.omega.omegamart.model.Order;
import com.omega.omegamart.model.OrderItem;
import com.omega.omegamart.util.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Orders and Order Items.
 * Supports multi-vendor marketplace queries and transactional order creation.
 */
public class OrderDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderDao.class);

    /**
     * Inserts an order within an existing transaction connection.
     */
    public Order createOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders (order_number, buyer_id, total_amount, status, shipping_address, payment_method) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getOrderNumber());
            stmt.setLong(2, order.getBuyerId());
            stmt.setBigDecimal(3, order.getTotalAmount());
            stmt.setString(4, order.getStatus());
            stmt.setString(5, order.getShippingAddress());
            stmt.setString(6, order.getPaymentMethod());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    order.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
            return order;
        }
    }

    /**
     * Inserts an individual order item within an existing transaction connection.
     */
    public OrderItem createOrderItem(Connection conn, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, seller_id, product_name, unit_price, quantity, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, item.getOrderId());
            stmt.setLong(2, item.getProductId());
            stmt.setLong(3, item.getSellerId());
            stmt.setString(4, item.getProductName());
            stmt.setBigDecimal(5, item.getUnitPrice());
            stmt.setInt(6, item.getQuantity());
            stmt.setBigDecimal(7, item.getSubtotal());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    item.setId(generatedKeys.getLong(1));
                }
            }
            return item;
        }
    }

    public Optional<Order> findById(Long orderId) {
        String sql = "SELECT o.id, o.order_number, o.buyer_id, o.total_amount, o.status, " +
                     "       o.shipping_address, o.payment_method, o.created_at, o.updated_at, " +
                     "       u.full_name AS buyer_name, u.email AS buyer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.buyer_id = u.id " +
                     "WHERE o.id = ?";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByOrderId(orderId));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding order by id {}: {}", orderId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Order> findByBuyerId(Long buyerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.order_number, o.buyer_id, o.total_amount, o.status, " +
                     "       o.shipping_address, o.payment_method, o.created_at, o.updated_at, " +
                     "       u.full_name AS buyer_name, u.email AS buyer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.buyer_id = u.id " +
                     "WHERE o.buyer_id = ? " +
                     "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setItems(findItemsByOrderId(order.getId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding orders for buyer {}: {}", buyerId, e.getMessage(), e);
        }
        return list;
    }

    /**
     * Retrieves orders containing products listed by the specified seller.
     * Only displays the order items belonging to this seller for multi-vendor isolation.
     */
    public List<Order> findBySellerId(Long sellerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT DISTINCT o.id, o.order_number, o.buyer_id, o.total_amount, o.status, " +
                     "                o.shipping_address, o.payment_method, o.created_at, o.updated_at, " +
                     "                u.full_name AS buyer_name, u.email AS buyer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.buyer_id = u.id " +
                     "JOIN order_items oi ON o.id = oi.order_id " +
                     "WHERE oi.seller_id = ? " +
                     "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    // Filter items to only show seller's own products
                    order.setItems(findItemsByOrderIdAndSellerId(order.getId(), sellerId));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding orders for seller {}: {}", sellerId, e.getMessage(), e);
        }
        return list;
    }

    public List<Order> findAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.id, o.order_number, o.buyer_id, o.total_amount, o.status, " +
                     "       o.shipping_address, o.payment_method, o.created_at, o.updated_at, " +
                     "       u.full_name AS buyer_name, u.email AS buyer_email " +
                     "FROM orders o " +
                     "JOIN users u ON o.buyer_id = u.id " +
                     "ORDER BY o.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(order.getId()));
                list.add(order);
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving all orders: {}", e.getMessage(), e);
        }
        return list;
    }

    public List<OrderItem> findItemsByOrderId(Long orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.seller_id, oi.product_name, " +
                     "       oi.unit_price, oi.quantity, oi.subtotal, u.full_name AS seller_name " +
                     "FROM order_items oi " +
                     "JOIN users u ON oi.seller_id = u.id " +
                     "WHERE oi.order_id = ? " +
                     "ORDER BY oi.id ASC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setSellerId(rs.getLong("seller_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setSellerName(rs.getString("seller_name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding order items for order {}: {}", orderId, e.getMessage(), e);
        }
        return items;
    }

    public List<OrderItem> findItemsByOrderIdAndSellerId(Long orderId, Long sellerId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.id, oi.order_id, oi.product_id, oi.seller_id, oi.product_name, " +
                     "       oi.unit_price, oi.quantity, oi.subtotal, u.full_name AS seller_name " +
                     "FROM order_items oi " +
                     "JOIN users u ON oi.seller_id = u.id " +
                     "WHERE oi.order_id = ? AND oi.seller_id = ? " +
                     "ORDER BY oi.id ASC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setSellerId(rs.getLong("seller_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setSellerName(rs.getString("seller_name"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding order items for order {} and seller {}: {}", orderId, sellerId, e.getMessage(), e);
        }
        return items;
    }

    /**
     * Checks if a buyer has a completed/confirmed order containing the specified product,
     * and returns the qualifying order ID (or null if not found).
     */
    public Optional<Long> getQualifyingOrderIdForReview(Long buyerId, Long productId) {
        String sql = "SELECT o.id FROM orders o " +
                     "JOIN order_items oi ON o.id = oi.order_id " +
                     "WHERE o.buyer_id = ? AND oi.product_id = ? AND o.status IN ('CONFIRMED', 'DELIVERED') " +
                     "LIMIT 1";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, buyerId);
            stmt.setLong(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking qualifying purchase for buyer {} and product {}: {}", buyerId, productId, e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setOrderNumber(rs.getString("order_number"));
        o.setBuyerId(rs.getLong("buyer_id"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setStatus(rs.getString("status"));
        o.setShippingAddress(rs.getString("shipping_address"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setCreatedAt(rs.getTimestamp("created_at"));
        o.setUpdatedAt(rs.getTimestamp("updated_at"));
        o.setBuyerName(rs.getString("buyer_name"));
        o.setBuyerEmail(rs.getString("buyer_email"));
        return o;
    }
}
