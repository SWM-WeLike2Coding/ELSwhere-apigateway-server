package com.wl2c.elswhereapigatewayserver.exception;

import org.springframework.http.HttpStatus;

public class LoggedOutAccessTokenException extends LocalizedMessageException {
    public LoggedOutAccessTokenException() { super(HttpStatus.BAD_REQUEST, "invalid.logged-out-token"); }
}
