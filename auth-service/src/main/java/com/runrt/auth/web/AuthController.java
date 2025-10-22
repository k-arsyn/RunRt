package com.runrt.auth.web;

import com.runrt.auth.domain.User;
import com.runrt.auth.domain.UserRepository;
import com.runrt.auth.security.AuthProperties;
import com.runrt.common.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthProperties properties;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username_taken"));
        }
        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("USER")
                .build();
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var userOpt = userRepository.findByUsername(req.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
        var user = userOpt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "invalid_credentials"));
        }
        String token = jwtUtil.generateToken(user.getUsername(), Map.of("uid", user.getId().toString(), "role", user.getRole()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
