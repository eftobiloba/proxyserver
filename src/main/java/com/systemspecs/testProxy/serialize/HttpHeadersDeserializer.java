package com.systemspecs.testProxy.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HttpHeadersDeserializer extends JsonDeserializer<HttpHeaders> {
    @Override
    public HttpHeaders deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Map<String, List<String>> headersMap = p.readValueAs(LinkedHashMap.class);
        HttpHeaders headers = new HttpHeaders();
        headersMap.forEach((key, values) -> headers.put(key, values));
        return headers;
    }
}
