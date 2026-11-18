package com.marabagam.common.exception;

public class ResourceNotFoundException extends AuthException {
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
