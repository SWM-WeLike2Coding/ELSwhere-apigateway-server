package com.wl2c.elswhereapigatewayserver.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends LocalizedMessageException {
    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, "invalid.token");
    }
}