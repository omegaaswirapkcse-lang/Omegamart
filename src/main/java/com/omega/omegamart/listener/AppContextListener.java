package com.omega.omegamart.listener;

import com.omega.omegamart.util.DatabaseConnectionPool;
import com.omega.omegamart.util.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * ServletContextListener managing application startup initialization (DB pool, schema, seed data)
 * and graceful shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("=================================================");
        LOGGER.info("Starting OmegaMart E-Commerce Platform...");
        LOGGER.info("=================================================");

        try {
            // 1. Initialize Connection Pool
            DatabaseConnectionPool.getInstance();

            // 2. Initialize Database Schema and Demonstration Data
            DatabaseInitializer.initialize();

            LOGGER.info("OmegaMart started successfully and ready to accept requests.");
        } catch (Exception e) {
            LOGGER.error("CRITICAL: Failed to initialize OmegaMart context: {}", e.getMessage(), e);
            throw new RuntimeException("Application startup failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down OmegaMart application context...");
        try {
            DatabaseConnectionPool.getInstance().shutdown();
            LOGGER.info("Database connection pool safely released.");
        } catch (Exception e) {
            LOGGER.error("Error during connection pool shutdown: {}", e.getMessage(), e);
        }
        LOGGER.info("OmegaMart context destroyed.");
    }
}
