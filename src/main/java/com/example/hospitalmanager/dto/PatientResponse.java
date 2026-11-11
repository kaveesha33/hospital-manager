
package com.example.hospitalmanager.dto;

import java.time.LocalDate;

public record PatientResponse(

    Long id,
    String fullName,
    LocalDate dateOfBirth,
    String gender,
    String phoneNumber,
    String email,
    String address

) {}
