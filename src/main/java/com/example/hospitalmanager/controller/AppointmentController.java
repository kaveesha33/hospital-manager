
package com.example.hospitalmanager.controller;

import com.example.hospitalmanager.dto.AppointmentRequest;
import com.example.hospitalmanager.dto.AppointmentResponse;
import com.example.hospitalmanager.dto.StatusRequest;

import com.example.hospitalmanager.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(
            AppointmentService appointmentService) {

        this.appointmentService = appointmentService;
    }

    // Book an appointment
    @PostMapping("/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse response =
                appointmentService.bookAppointment(
                        userDetails.getUsername(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Retrieve appointments
    @GetMapping({"/appointments", "/doctors/appointments"})
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<AppointmentResponse> appointments =
                appointmentService.getAppointments(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(appointments);
    }

    // Update appointment status
    @PatchMapping("/appointments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppointmentResponse response =
                appointmentService.updateAppointmentStatus(
                        id,
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(response);
    }
}
