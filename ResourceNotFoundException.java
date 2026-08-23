package com.splitwise.backend.exception;

/**
 * Thrown when a requested resource (user, group, expense, etc.) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
