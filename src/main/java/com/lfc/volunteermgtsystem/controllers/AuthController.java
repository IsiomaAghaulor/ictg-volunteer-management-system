package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.dto.AuthRequest;
import com.lfc.volunteermgtsystem.dto.AuthResponse;
import com.lfc.volunteermgtsystem.dto.UserRegistrationRequest;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.serviceImpls.UserService;
import com.lfc.volunteermgtsystem.utilities.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JwtUtil jwtUtil;
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);
            
            User user = (User) authentication.getPrincipal();
            userService.updateLastLogin(user.getEmail());

            
            return ResponseEntity.ok(new AuthResponse(jwt,
                    "Bearer",
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole()));
                    
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid credentials");
            error.put("message", "Email or password is incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationRequest signUpRequest) {
        try {
            if (userService.existsByEmail(signUpRequest.getEmail())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is already taken!");
                return ResponseEntity.badRequest().body(error);
            }
            
            User user = userService.createUser(signUpRequest);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("userId", user.getId().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User logged out successfully!");
        
        return ResponseEntity.ok(response);
    }
}