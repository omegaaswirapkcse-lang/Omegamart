package com.omega.omegamart.exception;

/**
 * Thrown when an authenticated user attempts an action outside their permissions/ownership.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
