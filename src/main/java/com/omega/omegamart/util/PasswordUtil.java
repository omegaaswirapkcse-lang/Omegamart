package com.omega.omegamart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Security utility for hashing and verifying passwords using jBCrypt.
 * Follows industry best practices: adaptive work factor and per-user salted hashing.
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 10;

    private PasswordUtil() {
        // Private constructor for utility class
    }

    /**
     * Hashes a plaintext password using BCrypt with a newly generated salt.
     *
     * @param plainTextPassword the password to hash
     * @return the resulting secure hash string
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies whether a plaintext candidate password matches an existing hash.
     *
     * @param plainTextPassword the candidate password
     * @param hashedPassword   the stored BCrypt hash
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
