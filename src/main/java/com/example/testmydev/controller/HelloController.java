package com.example.testmydev.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to TestMyDev Spring Boot Application!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        response.put("endpoints", Map.of(
            "hello", "/api/hello",
            "personalized_hello", "/api/hello/{name}",
            "health", "/api/health",
            "actuator_health", "/actuator/health",
            "h2_console", "/h2-console"
        ));
        return response;
    }

    @GetMapping("/api/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Spring Boot!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        return response;
    }

    @GetMapping("/api/hello/{name}")
    public Map<String, Object> helloName(@PathVariable String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "success");
        return response;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "testmydev");
        return response;
    }
}