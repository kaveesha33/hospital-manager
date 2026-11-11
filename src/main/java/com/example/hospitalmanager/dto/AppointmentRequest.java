
package com.example.hospitalmanager.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AppointmentRequest(

    @NotNull(message = "Doctor ID is required")
    Long doctorId,

    @NotNull(message = "Appointment date and time are required")
    @Future(message = "Appointment must be in the future")
    LocalDateTime appointmentDateTime

) {}
