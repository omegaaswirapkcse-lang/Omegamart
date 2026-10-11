package com.omega.omegamart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager loading application properties with environment variable overrides.
 */
public final class ConfigUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtil.class);
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                PROPERTIES.load(is);
            } else {
                LOGGER.warn("application.properties file not found on classpath; relying on defaults/environment.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load application.properties: {}", e.getMessage(), e);
        }
    }

    private ConfigUtil() {
    }

    /**
     * Retrieves a configuration property with fallback to environment variables and default values.
     *
     * @param key          property key (e.g., "db.url")
     * @param defaultValue fallback default value
     * @return resolved property value
     */
    public static String get(String key, String defaultValue) {
        // 1. Check system properties (-Dkey=value)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.trim().isEmpty()) {
            return sysProp.trim();
        }

        // 2. Check environment variable (convert "db.url" to "DB_URL")
        String envKey = key.toUpperCase().replace('.', '_');
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }

        // 3. Check loaded properties file
        String fileVal = PROPERTIES.getProperty(key);
        if (fileVal != null && !fileVal.trim().isEmpty()) {
            return fileVal.trim();
        }

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key, null);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid integer value '{}' for key '{}'; using default: {}", val, key, defaultValue);
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key, null);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val);
    }
}
