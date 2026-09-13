package com.erp.controller;

import com.erp.dto.request.ChangePasswordRequest;
import com.erp.dto.request.LoginRequest;
import com.erp.dto.request.RegisterRequest;
import com.erp.dto.response.AuthResponse;
import com.erp.dto.response.UserResponseDto;
import com.erp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login, registration, and profile management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login to the system", description = "Authenticates user credentials and returns JWT Bearer token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Register new employee account", description = "Creates a new user and employee profile (Admin/HR only)")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequest request) {
        UserResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Allows authenticated user to change their current password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns profile details for the currently authenticated user")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserProfile());
    }
}
