package com.omega.omegamart.dao;

import com.omega.omegamart.model.Category;
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
 * Data Access Object for Product Categories.
 */
public class CategoryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDao.class);

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description, created_at FROM categories ORDER BY name ASC";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching categories: {}", e.getMessage(), e);
        }
        return list;
    }

    public Optional<Category> findById(Long id) {
        String sql = "SELECT id, name, description, created_at FROM categories WHERE id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategory(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding category by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Category createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName().trim());
            stmt.setString(2, category.getDescription());
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getLong(1));
                }
            }
            return category;
        }
    }

    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setId(rs.getLong("id"));
        cat.setName(rs.getString("name"));
        cat.setDescription(rs.getString("description"));
        cat.setCreatedAt(rs.getTimestamp("created_at"));
        return cat;
    }
}
