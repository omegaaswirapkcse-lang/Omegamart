package com.omega.omegamart.dao;

import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
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
 * Data Access Object for User entities.
 */
public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, email, password_hash, full_name, role, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding user by id {}: {}", id, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, email, password_hash, full_name, role, created_at, updated_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding user by email {}: {}", email, e.getMessage(), e);
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, full_name, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getEmail().trim().toLowerCase());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName().trim());
            stmt.setString(4, user.getRole().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        }
    }

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, email, password_hash, full_name, role, created_at, updated_at FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnectionPool.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching all users: {}", e.getMessage(), e);
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(Role.fromString(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
