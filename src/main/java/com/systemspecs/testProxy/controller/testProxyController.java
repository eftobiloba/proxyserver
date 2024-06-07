package com.systemspecs.testProxy.controller;

// Necessary imports
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;

// Marks the class as a REST controller
@RestController
// Base URL for all routes in this controller
@RequestMapping("/proxy")
public class testProxyController {
    // Logger instance for logging events
    private static final Logger logger = LoggerFactory.getLogger(testProxyController.class);

    // Injects RestTemplate bean
    @Autowired
    private RestTemplate restTemplate;

    // Handles routing of requests
    @RequestMapping("/router/")
    public ResponseEntity<?> routeRequest(
            @RequestHeader Map<String, String> header,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request
    ) {
        // Retrieve the URL from headers
        String url = header.get("url");
        // Return a bad request if URL is missing
        if (url == null) {
            return ResponseEntity.badRequest().body("Missing URL in headers");
        }

        // Get the HTTP method of the incoming request
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

        try {
            // Initialize headers for the outgoing request
            HttpHeaders headers = new HttpHeaders();
            // Remove the URL from headers as it's not needed in the forwarded request
            header.remove("url");

            // Copy all headers except CONTENT_LENGTH and HOST to the outgoing request
            header.forEach((key, value) -> {
                if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !key.equalsIgnoreCase(HttpHeaders.HOST)) {
                    headers.set(key, value);
                }
            });

            // Prepare the entity for the outgoing request
            HttpEntity<?> requestEntity;
            if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
                requestEntity = new HttpEntity<>(headers);
            } else if (body != null) {
                requestEntity = new HttpEntity<>(body, headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            // Send the request to the specified URL
            ResponseEntity<byte[]> response = restTemplate.exchange(url, method, requestEntity, byte[].class);

            // Initialize headers for the response
            HttpHeaders responseHeaders = new HttpHeaders();
            // Copy all headers except TRANSFER_ENCODING to the response
            response.getHeaders().forEach((key, value) -> {
                if (!key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                    responseHeaders.put(key, value);
                }
            });

            // Return the response from the external service to the client
            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            // Log the exception and return the status code and body of the error response
            logger.error("HttpStatusCodeException: {}", ex.getResponseBodyAsString(), ex);
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }
}