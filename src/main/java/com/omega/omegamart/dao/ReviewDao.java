package com.omega.omegamart.dao;

import com.omega.omegamart.model.Review;
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

/**
 * Data Access Object for Product Reviews and Ratings.
 */
public class ReviewDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewDao.class);

    public List<Review> findByProductId(Long productId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.id, r.product_id, r.user_id, r.order_id, r.rating, r.comment, r.created_at, " +
                     "       u.full_name AS user_name, p.name AS product_name " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "JOIN products p ON r.product_id = p.id " +
                     "WHERE r.product_id = ? " +
                     "ORDER BY r.created_at DESC";

        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setProductId(rs.getLong("product_id"));
                    r.setUserId(rs.getLong("user_id"));
                    r.setOrderId(rs.getLong("order_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setUserName(rs.getString("user_name"));
                    r.setProductName(rs.getString("product_name"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding reviews for product {}: {}", productId, e.getMessage(), e);
        }
        return list;
    }

    public Review createReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, review.getProductId());
            stmt.setLong(2, review.getUserId());
            stmt.setLong(3, review.getOrderId());
            stmt.setInt(4, review.getRating());
            stmt.setString(5, review.getComment().trim());

            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setId(generatedKeys.getLong(1));
                }
            }
            return review;
        }
    }

    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking review existence for user {} and product {}: {}", userId, productId, e.getMessage(), e);
        }
        return false;
    }
}
