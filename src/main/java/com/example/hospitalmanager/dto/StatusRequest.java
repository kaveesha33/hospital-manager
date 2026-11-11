
package com.example.hospitalmanager.dto;

import com.example.hospitalmanager.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(

    @NotNull(message = "Appointment status is required")
    AppointmentStatus status

) {}
