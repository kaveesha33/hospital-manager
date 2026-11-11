
package com.example.hospitalmanager.dto;

import java.time.LocalDateTime;

public record MedicalRecordResponse(

    Long id,

    Long patientId,

    String patientName,

    Long doctorId,

    String doctorName,

    String diagnosis,

    String prescription,

    LocalDateTime createdAt

) {}
