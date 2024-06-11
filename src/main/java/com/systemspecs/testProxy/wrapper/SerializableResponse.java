package com.systemspecs.testProxy.wrapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SerializableResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private byte[] body;
    private int statusCode;
    private Map<String, List<String>> headers;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    // Empty constructor for Jackson deserialization
    public SerializableResponse() {}

    // Constructor for creating SerializableResponse objects from ResponseEntity
    @JsonCreator
    public SerializableResponse(
            @JsonProperty("body") byte[] body,
            @JsonProperty("statusCode") int statusCode,
            @JsonProperty("headers") Map<String, List<String>> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, List<String>> headers){
        if (this.headers != null) {
            httpHeaders.putAll(headers);
        }
    }

    public static SerializableResponse fromResponseEntity(ResponseEntity<byte[]> responseEntity) {
        return new SerializableResponse(
                responseEntity.getBody(),
                responseEntity.getStatusCodeValue(),
                responseEntity.getHeaders().toSingleValueMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> Collections.singletonList(e.getValue())))
        );
    }

    public ResponseEntity<byte[]> toResponseEntity() {
        this.setHttpHeaders(this.headers);
        return new ResponseEntity<>(this.body, this.getHttpHeaders(), HttpStatus.valueOf(this.statusCode));
    }
}
