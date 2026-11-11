
package com.example.hospitalmanager.controller;

import com.example.hospitalmanager.dto.RegisterRequest;
import com.example.hospitalmanager.dto.UserResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.Role;

import com.example.hospitalmanager.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StaffController {

    private final AuthService authService;

    public StaffController(AuthService authService) {
        this.authService = authService;
    }

    // List all registered users
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(authService.getAllUsers());
    }

    // List available doctors
    @GetMapping("/doctors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getAllDoctors() {

        return ResponseEntity.ok(authService.getAllDoctors());
    }

    // Register a new doctor
    @PostMapping({"/doctors", "/admin/doctors"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> registerDoctor(
            @Valid @RequestBody RegisterRequest request) {

        if (request.role() != Role.DOCTOR) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "This endpoint only accepts the DOCTOR role"
            );
        }

        UserResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
