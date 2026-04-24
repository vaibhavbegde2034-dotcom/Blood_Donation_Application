package com.blooddonation.controller;

import com.blooddonation.dto.LoginDto;
import com.blooddonation.dto.UserRegistrationDto;
import com.blooddonation.model.User;
import com.blooddonation.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        System.out.println("Registering user: " + registrationDto.getUsername());
        try {
            userService.registerUser(registrationDto);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
            throw e; // Rethrow to be caught by GlobalExceptionHandler
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDto loginDto) {
        System.out.println("Login attempt for: " + loginDto.getUsernameOrEmail());
        Optional<User> userOptional = userService.findByUsernameOrEmail(loginDto.getUsernameOrEmail());
        
        if (userOptional.isPresent()) {
            boolean matches = passwordEncoder.matches(loginDto.getPassword(), userOptional.get().getPassword());
            System.out.println("User found. Password matches: " + matches);
            if (matches) {
                User user = userOptional.get();
                Map<String, String> response = new HashMap<>();
                response.put("message", "Login successful");
                response.put("username", user.getUsername());
                response.put("fullName", user.getFullName() != null ? user.getFullName() : "");
                response.put("email", user.getEmail());
                response.put("id", user.getId().toString());
                return ResponseEntity.ok(response);
            }
        } else {
            System.out.println("User not found with username or email: " + loginDto.getUsernameOrEmail());
        }
        return ResponseEntity.status(401).body("Invalid username or password");
    }
}
