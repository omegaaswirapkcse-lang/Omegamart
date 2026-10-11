package com.omega.omegamart.exception;

/**
 * Thrown when credentials fail or account cannot be authenticated.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
