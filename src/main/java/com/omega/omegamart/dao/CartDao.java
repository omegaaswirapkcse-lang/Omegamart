package com.omega.omegamart.dao;

import com.omega.omegamart.model.CartItem;
import com.omega.omegamart.model.Product;
import com.omega.omegamart.util.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for shopping cart operations.
 */
public class CartDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartDao.class);

    public List<CartItem> getCartItems(Long userId) {
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT c.id, c.user_id, c.product_id, c.quantity, c.created_at, " +
                     "       p.name, p.description, p.price, p.stock_quantity, p.image_url, p.is_active, p.seller_id " +
                     "FROM cart_items c " +
                     "JOIN products p ON c.product_id = p.id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY c.created_at ASC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setUserId(rs.getLong("user_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setCreatedAt(rs.getTimestamp("created_at"));

                    Product product = new Product();
                    product.setId(rs.getLong("product_id"));
                    product.setName(rs.getString("name"));
                    product.setDescription(rs.getString("description"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setStockQuantity(rs.getInt("stock_quantity"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setIsActive(rs.getBoolean("is_active"));
                    product.setSellerId(rs.getLong("seller_id"));

                    item.setProduct(product);
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving cart items for user {}: {}", userId, e.getMessage(), e);
        }
        return items;
    }

    public void addToCart(Long userId, Long productId, int quantity) throws SQLException {
        String checkSql = "SELECT id, quantity FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setLong(1, userId);
            checkStmt.setLong(2, productId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int existingQty = rs.getInt("quantity");
                    int newQty = existingQty + quantity;
                    String updateSql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newQty);
                        updateStmt.setLong(2, userId);
                        updateStmt.setLong(3, productId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertSql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, userId);
                        insertStmt.setLong(2, productId);
                        insertStmt.setInt(3, quantity);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    public void updateQuantity(Long userId, Long productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
            return;
        }
        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, userId);
            stmt.setLong(3, productId);
            stmt.executeUpdate();
        }
    }

    public void removeFromCart(Long userId, Long productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
        }
    }

    public void clearCart(Connection conn, Long userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }

    public int getCartCount(Long userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error getting cart count for user {}: {}", userId, e.getMessage(), e);
        }
        return 0;
    }
}
