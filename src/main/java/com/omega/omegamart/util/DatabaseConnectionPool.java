package com.omega.omegamart.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton database connection pool manager utilizing HikariCP.
 * Provides high-performance, robust connection lifecycle management.
 */
public final class DatabaseConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConnectionPool.class);

    private static volatile DatabaseConnectionPool instance;
    private final HikariDataSource dataSource;

    private DatabaseConnectionPool() {
        this(null);
    }

    private DatabaseConnectionPool(HikariConfig customConfig) {
        if (customConfig != null) {
            this.dataSource = new HikariDataSource(customConfig);
        } else {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(ConfigUtil.get("db.driver", "org.h2.Driver"));
            config.setJdbcUrl(ConfigUtil.get("db.url", "jdbc:h2:file:./data/omegamartdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"));
            config.setUsername(ConfigUtil.get("db.user", "sa"));
            config.setPassword(ConfigUtil.get("db.password", ""));

            config.setMaximumPoolSize(ConfigUtil.getInt("db.pool.maxSize", 10));
            config.setMinimumIdle(ConfigUtil.getInt("db.pool.minIdle", 2));
            config.setIdleTimeout(ConfigUtil.getInt("db.pool.idleTimeout", 30000));
            config.setConnectionTimeout(ConfigUtil.getInt("db.pool.connectionTimeout", 20000));
            config.setPoolName("OmegaMartHikariPool");

            // Recommended HikariCP caching properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            this.dataSource = new HikariDataSource(config);
            LOGGER.info("HikariCP connection pool initialized for URL: {}", config.getJdbcUrl());
        }
    }

    /**
     * Retrieves the singleton DatabaseConnectionPool instance.
     *
     * @return the singleton connection pool instance
     */
    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }

    /**
     * Allows initializing the connection pool with a custom configuration (e.g. for testing with H2 in-memory).
     *
     * @param customConfig custom HikariConfig
     * @return initialized DatabaseConnectionPool
     */
    public static synchronized DatabaseConnectionPool initializeWithConfig(HikariConfig customConfig) {
        if (instance != null) {
            instance.shutdown();
        }
        instance = new DatabaseConnectionPool(customConfig);
        return instance;
    }

    /**
     * Obtains an active connection from the pool. Must be closed with try-with-resources.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection cannot be acquired
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Retrieves the underlying javax.sql.DataSource.
     *
     * @return the DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Shuts down the HikariCP connection pool gracefully.
     */
    public synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            LOGGER.info("Closing HikariCP connection pool...");
            dataSource.close();
            LOGGER.info("HikariCP connection pool successfully shut down.");
        }
        instance = null;
    }
}
