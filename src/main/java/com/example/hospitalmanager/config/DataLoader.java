
package com.example.hospitalmanager.config;

import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;

import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-password}")
    private String adminPassword;

    @Value("${app.seed.doctor-password}")
    private String doctorPassword;

    @Value("${app.seed.patient-password}")
    private String patientPassword;

    public DataLoader(
            UserRepository userRepository,
            PatientRepository patientRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Create initial administrator
        createUser(
                "Hospital Administrator",
                "admin01",
                adminPassword,
                Role.ADMIN
        );

        // Create initial doctor
        createUser(
                "Dr. Perera",
                "doctor01",
                doctorPassword,
                Role.DOCTOR
        );

        // Create initial patient account
        createUser(
                "John Silva",
                "patient01@hospital.test",
                patientPassword,
                Role.PATIENT
        );

        // Create a patient profile if it doesn't already exist
        createPatientProfile();
    }

    private void createUser(
            String name,
            String username,
            String password,
            Role role) {

        if (userRepository.existsByUsername(username)) {
            return;
        }

        AppUser user = new AppUser(
                name,
                username,
                passwordEncoder.encode(password),
                role
        );

        userRepository.save(user);

        System.out.println("Created initial user: " + username);
    }

    private void createPatientProfile() {

        String email = "patient01@hospital.test";

        if (patientRepository.existsByEmail(email)) {
            return;
        }

        Patient patient = new Patient();

        patient.setFullName("John Silva");
        patient.setDateOfBirth(LocalDate.of(1995, 5, 15));
        patient.setGender("Male");
        patient.setPhoneNumber("0771234567");
        patient.setEmail(email);
        patient.setAddress("Colombo, Sri Lanka");

        patientRepository.save(patient);

        System.out.println("Created initial patient profile.");
    }
}
