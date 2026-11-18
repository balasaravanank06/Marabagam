package com.marabagam.common.exception;

public class InvalidInputException extends AuthException {
    public InvalidInputException(String message) {
        super("INVALID_INPUT", message);
    }
}
