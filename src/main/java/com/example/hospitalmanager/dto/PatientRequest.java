
package com.example.hospitalmanager.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record PatientRequest(

    @NotBlank(message = "Patient name is required")
    @Size(max = 100)
    String fullName,

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @NotBlank(message = "Gender is required")
    String gender,

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    String phoneNumber,

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    String email,

    @NotBlank(message = "Address is required")
    @Size(max = 255)
    String address

) {}
