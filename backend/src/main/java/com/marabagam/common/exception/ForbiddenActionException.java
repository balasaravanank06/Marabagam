package com.marabagam.common.exception;

public class ForbiddenActionException extends AuthException {
    public ForbiddenActionException(String message) {
        super("FORBIDDEN_ACTION", message);
    }
}
