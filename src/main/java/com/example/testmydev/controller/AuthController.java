package com.example.testmydev.controller;

import com.example.testmydev.model.User;
import com.example.testmydev.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        User user = new User();
        user.setUsername(body.get("username"));
        user.setPassword(body.get("password"));
        user.setName(body.get("name"));
        user.setEmail(body.get("email"));
        user.setAddress(body.get("address"));

        try {
            User saved = userService.signup(user);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Account created successfully",
                "username", saved.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> user = userService.login(username, password);
        if (user.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Login successful",
                "username", user.get().getUsername(),
                "name", user.get().getName()
            ));
        }
        return ResponseEntity.status(401).body(Map.of(
            "status", "error",
            "message", "Invalid username or password"
        ));
    }
}
