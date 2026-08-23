package com.splitwise.backend.exception;

/**
 * Thrown when a request is invalid, e.g. missing required fields
 * or referencing users that are not part of a group.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
