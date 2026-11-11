
package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.*;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.UserRepository;
import com.example.hospitalmanager.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // Register a new user
    @Transactional
    public UserResponse register(RegisterRequest request) {

        if (request.role() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Role is required"
            );
        }

        // Only PATIENT accounts can self-register.
        // Staff accounts must be registered by an ADMIN.
        if (request.role() != Role.PATIENT && !isAdmin()) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can register staff accounts"
            );
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Username is already registered"
            );
        }

        AppUser user = new AppUser(
                request.name(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        AppUser savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    // Authenticate user and generate JWT
    public AuthResponse login(LoginRequest request) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

        } catch (AuthenticationException ex) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(
                userDetails,
                user.getRole()
        );

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpiration() / 1000,
                user.getRole()
        );
    }

    // Get currently authenticated user
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        return mapToUserResponse(user);
    }

    // List all users
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // List all doctors
    @Transactional(readOnly = true)
    public List<UserResponse> getAllDoctors() {

        return userRepository.findByRole(Role.DOCTOR)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    // Check whether the current user is an administrator
    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority.getAuthority().equals("ROLE_ADMIN")
                    );
    }

    // Convert AppUser entity into UserResponse DTO
    private UserResponse mapToUserResponse(AppUser user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRole()
        );
    }
}
