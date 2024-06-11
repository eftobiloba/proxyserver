package com.systemspecs.testProxy.controller;

// Necessary imports
import com.systemspecs.testProxy.wrapper.SerializableResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Objects;

// Marks the class as a REST controller
@RestController
@RequestMapping("/proxy")
public class testProxyController {
    private static final Logger logger = LoggerFactory.getLogger(testProxyController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping("/router/")
    public ResponseEntity<?> routeRequest(
            @RequestHeader Map<String, String> header,
            @RequestBody(required = false) Map<String, Object> body,
            HttpServletRequest request
    ) {
        String url = header.get("url");
        if (url == null) {
            return ResponseEntity.badRequest().body("Missing URL in headers");
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());

        try {
            if (method == HttpMethod.GET) {
                // Check cache first
                Cache cache = cacheManager.getCache("proxyResponses");
                assert cache != null;
                SerializableResponse cachedResponse = cache.get(url, SerializableResponse.class);
                if (cachedResponse != null) {
                    return cachedResponse.toResponseEntity();
                }
            }

            HttpHeaders headers = new HttpHeaders();
            header.remove("url");
            header.forEach((key, value) -> {
                if (!key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) && !key.equalsIgnoreCase(HttpHeaders.HOST)) {
                    headers.set(key, value);
                }
            });

            HttpEntity<?> requestEntity;
            if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
                requestEntity = new HttpEntity<>(headers);
            } else if (body != null) {
                requestEntity = new HttpEntity<>(body, headers);
            } else {
                requestEntity = new HttpEntity<>(headers);
            }

            ResponseEntity<byte[]> response = restTemplate.exchange(url, method, requestEntity, byte[].class);

            if (method == HttpMethod.GET) {
                // Cache the response
                SerializableResponse serializableResponse = SerializableResponse.fromResponseEntity(response);
                Objects.requireNonNull(cacheManager.getCache("proxyResponses")).put(url, serializableResponse);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, value) -> {
                if (!key.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                    responseHeaders.put(key, value);
                }
            });

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            logger.error("HttpStatusCodeException: {}", ex.getResponseBodyAsString(), ex);
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAsString());
        }
    }
}
