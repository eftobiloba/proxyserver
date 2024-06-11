package com.systemspecs.testProxy.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Map;

@Component("urlKeyGenerator")
public class UrlKeyGenerator implements KeyGenerator{

    @Override
    public Object generate(Object target, Method method, Object... params){
        HttpServletRequest request = (HttpServletRequest) params[2];
        Map<String, String> header = (Map<String, String>) params[0];
        return header.get("url");
    }
}
