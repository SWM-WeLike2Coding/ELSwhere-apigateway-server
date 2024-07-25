package com.wl2c.elswhereapigatewayserver.exception;

import org.springframework.http.HttpStatus;

public class NotGrantedException extends LocalizedMessageException {
    public NotGrantedException() {
        super(HttpStatus.UNAUTHORIZED, "required.granted");
    }
}
