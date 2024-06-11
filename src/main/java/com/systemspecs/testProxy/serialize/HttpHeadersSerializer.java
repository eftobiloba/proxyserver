package com.systemspecs.testProxy.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

public class HttpHeadersSerializer extends JsonSerializer<HttpHeaders> {
    @Override
    public void serialize(HttpHeaders headers, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(headers.toSingleValueMap());
    }
}
