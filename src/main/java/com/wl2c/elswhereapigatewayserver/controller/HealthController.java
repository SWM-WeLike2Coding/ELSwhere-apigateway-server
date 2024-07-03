package com.wl2c.elswhereapigatewayserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Slf4j
public class HealthController {

    @Value("${server.health.header.key}")
    private String HEADER_KEY;

    @Value("${server.health.header.value}")
    private String HEADER_VALUE;

    /**
     * health check
     */
    @GetMapping
    public ResponseEntity<Void> healthCheck(@RequestHeader MultiValueMap<String, String> headers) {
        headers.forEach((key, value) -> log.info("Header: {}={}", key, value));

        if (headers.containsKey(HEADER_KEY)) {
            String authHeaderValue = headers.getFirst(HEADER_KEY);
            if (authHeaderValue.equals(HEADER_VALUE)) {
                log.info("health check header is valid.");
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                log.error("health check header is invalid.");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            log.error("health check header not found.");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
