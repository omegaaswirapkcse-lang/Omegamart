package com.omega.omegamart.exception;

/**
 * Thrown when a requested resource (product, order, user) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
