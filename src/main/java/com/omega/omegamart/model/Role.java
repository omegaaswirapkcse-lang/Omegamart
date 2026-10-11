package com.omega.omegamart.model;

/**
 * Enumeration representing user roles in OmegaMart.
 */
public enum Role {
    BUYER,
    SELLER,
    ADMIN;

    public static Role fromString(String roleStr) {
        if (roleStr == null) {
            return null;
        }
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
