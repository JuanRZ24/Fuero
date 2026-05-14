package com.lexflow.api.controller;

import com.lexflow.api.dto.AuthResponse;
import com.lexflow.api.dto.LoginRequest;
import com.lexflow.api.dto.RegistroDespachoRequest;
import com.lexflow.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder().mensaje(e.getMessage()).build());
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrarDespacho(@RequestBody RegistroDespachoRequest request) {
        AuthResponse response = authService.registerNewDespacho(request);
        return ResponseEntity.ok(response);
    }
}