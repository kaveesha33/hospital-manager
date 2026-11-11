
package com.example.hospitalmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicalRecordRequest(

    @NotNull(message = "Patient ID is required")
    Long patientId,

    @NotBlank(message = "Diagnosis is required")
    String diagnosis,

    @NotBlank(message = "Prescription is required")
    String prescription

) {}
