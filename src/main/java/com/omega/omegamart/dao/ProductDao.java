package com.omega.omegamart.dao;

import com.omega.omegamart.model.Product;
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
 * Data Access Object for Product catalog and seller inventory operations.
 */
public class ProductDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductDao.class);

    private static final String BASE_SELECT =
            "SELECT p.id, p.seller_id, p.category_id, p.name, p.description, p.price, " +
            "       p.stock_quantity, p.image_url, p.is_active, p.created_at, p.updated_at, " +
            "       u.full_name AS seller_name, c.name AS category_name, " +
            "       COALESCE(AVG(r.rating), 0.0) AS avg_rating, " +
            "       COUNT(r.id) AS review_count " +
            "FROM products p " +
            "JOIN users u ON p.seller_id = u.id " +
            "JOIN categories c ON p.category_id = c.id " +
            "LEFT JOIN reviews r ON p.id = r.product_id ";

    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT +
                "WHERE p.id = ? " +
                "GROUP BY p.id, p.seller_id, p.category_id, p.name, p.description, p.price, " +
                "         p.stock_quantity, p.image_url, p.is_active, p.created_at, p.updated_at, " +
                "         u.full_name, c.name";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding product by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Product> findAll(String searchKeyword, Long categoryId, int offset, int limit) {
        List<Product> products = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE p.is_active = TRUE ");

        List<Object> params = new ArrayList<>();
        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String wildcard = "%" + searchKeyword.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }

        sql.append("GROUP BY p.id, p.seller_id, p.category_id, p.name, p.description, p.price, ")
           .append("         p.stock_quantity, p.image_url, p.is_active, p.created_at, p.updated_at, ")
           .append("         u.full_name, c.name ")
           .append("ORDER BY p.created_at DESC ")
           .append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error searching products: {}", e.getMessage(), e);
        }
        return products;
    }

    public int countAll(String searchKeyword, Long categoryId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE p.is_active = TRUE ");
        List<Object> params = new ArrayList<>();

        if (categoryId != null && categoryId > 0) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ?) ");
            String wildcard = "%" + searchKeyword.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error counting products: {}", e.getMessage(), e);
        }
        return 0;
    }

    public List<Product> findBySellerId(Long sellerId) {
        List<Product> products = new ArrayList<>();
        String sql = BASE_SELECT +
                "WHERE p.seller_id = ? " +
                "GROUP BY p.id, p.seller_id, p.category_id, p.name, p.description, p.price, " +
                "         p.stock_quantity, p.image_url, p.is_active, p.created_at, p.updated_at, " +
                "         u.full_name, c.name " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding products for seller {}: {}", sellerId, e.getMessage(), e);
        }
        return products;
    }

    public List<Product> findAllForAdmin() {
        List<Product> products = new ArrayList<>();
        String sql = BASE_SELECT +
                "GROUP BY p.id, p.seller_id, p.category_id, p.name, p.description, p.price, " +
                "         p.stock_quantity, p.image_url, p.is_active, p.created_at, p.updated_at, " +
                "         u.full_name, c.name " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding all products for admin: {}", e.getMessage(), e);
        }
        return products;
    }

    public Product createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (seller_id, category_id, name, description, price, stock_quantity, image_url, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, product.getSellerId());
            stmt.setLong(2, product.getCategoryId());
            stmt.setString(3, product.getName().trim());
            stmt.setString(4, product.getDescription());
            stmt.setBigDecimal(5, product.getPrice());
            stmt.setInt(6, product.getStockQuantity());
            stmt.setString(7, product.getImageUrl());
            stmt.setBoolean(8, product.getIsActive() != null ? product.getIsActive() : true);

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                }
            }
            return product;
        }
    }

    /**
     * Enforces seller ownership during update.
     */
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, description = ?, price = ?, " +
                     "stock_quantity = ?, image_url = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND seller_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, product.getCategoryId());
            stmt.setString(2, product.getName().trim());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setString(6, product.getImageUrl());
            stmt.setBoolean(7, product.getIsActive() != null ? product.getIsActive() : true);
            stmt.setLong(8, product.getId());
            stmt.setLong(9, product.getSellerId());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes or soft-deactivates product enforcing seller ownership.
     */
    public boolean deleteProduct(Long productId, Long sellerId) throws SQLException {
        // If referenced in order_items, soft-deactivate instead of hard delete to preserve historical integrity
        if (isProductInOrders(productId)) {
            String sql = "UPDATE products SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND seller_id = ?";
            try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, productId);
                stmt.setLong(2, sellerId);
                return stmt.executeUpdate() > 0;
            }
        } else {
            String sql = "DELETE FROM products WHERE id = ? AND seller_id = ?";
            try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, productId);
                stmt.setLong(2, sellerId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    /**
     * Admin moderation removal/deactivation.
     */
    public boolean adminDeleteProduct(Long productId) throws SQLException {
        if (isProductInOrders(productId)) {
            String sql = "UPDATE products SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, productId);
                return stmt.executeUpdate() > 0;
            }
        } else {
            String sql = "DELETE FROM products WHERE id = ?";
            try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, productId);
                return stmt.executeUpdate() > 0;
            }
        }
    }

    private boolean isProductInOrders(Long productId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking order references for product {}: {}", productId, e.getMessage(), e);
        }
        return false;
    }

    public boolean updateStock(Connection conn, Long productId, int newStock) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setLong(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setSellerId(rs.getLong("seller_id"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setIsActive(rs.getBoolean("is_active"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        p.setUpdatedAt(rs.getTimestamp("updated_at"));
        p.setSellerName(rs.getString("seller_name"));
        p.setCategoryName(rs.getString("category_name"));
        p.setAverageRating(rs.getDouble("avg_rating"));
        p.setReviewCount(rs.getInt("review_count"));
        return p;
    }
}
