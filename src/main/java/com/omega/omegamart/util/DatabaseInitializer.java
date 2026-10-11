
package com.omega.omegamart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes the OmegaMart database and default accounts.
 */
public final class DatabaseInitializer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    public static void initialize() {
        LOGGER.info("Starting OmegaMart database initialization...");

        try (Connection conn =
                     DatabaseConnectionPool.getInstance().getConnection()) {

            executeSqlScript(conn, "db/schema.sql");
            seedInitialData(conn);
            syncUserSequence(conn);
            ensureDefaultAccounts(conn);
            syncUserSequence(conn);

            LOGGER.info("OmegaMart database initialized successfully.");

        } catch (SQLException | IOException e) {
            LOGGER.error("Database initialization failed", e);
            throw new RuntimeException(
                    "Could not initialize database", e);
        }
    }

    public static void executeSqlScript(
            Connection conn, String scriptPath)
            throws IOException, SQLException {

        LOGGER.info("Executing SQL script: {}", scriptPath);

        try (InputStream is =
                     DatabaseInitializer.class.getClassLoader()
                             .getResourceAsStream(scriptPath)) {

            if (is == null) {
                LOGGER.warn("Script '{}' not found on classpath.",
                        scriptPath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            is, StandardCharsets.UTF_8));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sqlBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();

                    if (trimmed.startsWith("--")
                            || trimmed.isEmpty()) {
                        continue;
                    }

                    sqlBuilder.append(line).append("\n");

                    if (trimmed.endsWith(";")) {
                        String sql = sqlBuilder.toString().trim();
                        sql = sql.substring(0, sql.length() - 1).trim();

                        if (!sql.isEmpty()) {
                            stmt.execute(sql);
                        }

                        sqlBuilder.setLength(0);
                    }
                }

                // Execute a final statement if it has no trailing semicolon.
                String remaining = sqlBuilder.toString().trim();
                if (!remaining.isEmpty()) {
                    stmt.execute(remaining);
                }
            }
        }
    }

    private static void seedInitialData(Connection conn)
            throws IOException, SQLException {

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM categories")) {

            if (rs.next() && rs.getInt(1) == 0) {
                LOGGER.info(
                        "Categories table is empty. Executing seed data...");
                executeSqlScript(conn, "db/seed.sql");
            } else {
                LOGGER.info(
                        "Existing database data found. Skipping full seed reload.");
            }
        }
    }

    /**
     * Synchronizes the users table's auto-generated ID counter.
     */
    private static void syncUserSequence(Connection conn)
            throws SQLException {

        long nextId;

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COALESCE(MAX(id), 0) + 1 FROM users")) {

            if (!rs.next()) {
                throw new SQLException(
                        "Could not determine the next user ID.");
            }

            nextId = rs.getLong(1);
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(
                    "ALTER TABLE users ALTER COLUMN id RESTART WITH "
                            + nextId);
        }

        LOGGER.info(
                "User ID sequence synchronized. Next ID: {}", nextId);
    }

    private static void ensureDefaultAccounts(Connection conn)
            throws SQLException {

        ensureUser(conn, "admin@omegamart.com",
                "Admin@12345", "System Administrator", "ADMIN");

        ensureUser(conn, "seller1@omegamart.com",
                "Seller@12345", "Apex Electronics Store", "SELLER");

        ensureUser(conn, "seller2@omegamart.com",
                "Seller@12345", "Urban Threads Boutique", "SELLER");

        ensureUser(conn, "buyer1@omegamart.com",
                "Buyer@12345", "Alice Johnson", "BUYER");

        ensureUser(conn, "buyer2@omegamart.com",
                "Buyer@12345", "Bob Williams", "BUYER");
    }

    private static void ensureUser(
            Connection conn,
            String email,
            String plainPassword,
            String fullName,
            String role) throws SQLException {

        String selectSql =
                "SELECT id, password_hash FROM users WHERE email = ?";

        try (PreparedStatement selectStmt =
                     conn.prepareStatement(selectSql)) {

            selectStmt.setString(1, email);

            try (ResultSet rs = selectStmt.executeQuery()) {

                if (!rs.next()) {
                    String insertSql =
                            "INSERT INTO users "
                                    + "(email, password_hash, full_name, role) "
                                    + "VALUES (?, ?, ?, ?)";

                    try (PreparedStatement insertStmt =
                                 conn.prepareStatement(insertSql)) {

                        insertStmt.setString(1, email);
                        insertStmt.setString(
                                2, PasswordUtil.hashPassword(plainPassword));
                        insertStmt.setString(3, fullName);
                        insertStmt.setString(4, role);

                        insertStmt.executeUpdate();

                        LOGGER.info(
                                "Created default user account: {} ({})",
                                email, role);
                    }

                } else {
                    String currentHash = rs.getString("password_hash");

                    if (currentHash == null
                            || !PasswordUtil.checkPassword(
                                    plainPassword, currentHash)) {

                        String updateSql =
                                "UPDATE users SET password_hash = ? "
                                        + "WHERE email = ?";

                        try (PreparedStatement updateStmt =
                                     conn.prepareStatement(updateSql)) {

                            updateStmt.setString(
                                    1, PasswordUtil.hashPassword(
                                            plainPassword));
                            updateStmt.setString(2, email);
                            updateStmt.executeUpdate();

                            LOGGER.info(
                                    "Updated password hash for default user: {}",
                                    email);
                        }
                    }
                }
            }
        }
    }
}
