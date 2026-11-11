
package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.AppointmentRequest;
import com.example.hospitalmanager.dto.AppointmentResponse;
import com.example.hospitalmanager.dto.StatusRequest;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.Appointment;
import com.example.hospitalmanager.model.AppointmentStatus;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.AppointmentRepository;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            UserRepository userRepository) {

        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    // Book an appointment for the authenticated patient
    @Transactional
    public AppointmentResponse bookAppointment(
            String username,
            AppointmentRequest request) {

        AppUser patientUser = findUser(username);

        if (patientUser.getRole() != Role.PATIENT) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only patients can book appointments"
            );
        }

        // Patient username must match the patient's profile email
        Patient patient = patientRepository.findByEmail(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient profile not found for this account"
                ));

        if (request.appointmentDateTime() == null ||
                !request.appointmentDateTime().isAfter(LocalDateTime.now())) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment date and time must be in the future"
            );
        }

        AppUser doctor = userRepository.findById(request.doctorId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Doctor not found"
                ));

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Selected user is not a doctor"
            );
        }

        Appointment appointment = new Appointment();

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(
                request.appointmentDateTime()
        );
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        return mapToResponse(savedAppointment);
    }

    // Retrieve appointments based on the current user's role
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointments(String username) {

        AppUser user = findUser(username);

        List<Appointment> appointments;

        if (user.getRole() == Role.ADMIN) {

            appointments = appointmentRepository.findAll();

        } else if (user.getRole() == Role.DOCTOR) {

            appointments =
                    appointmentRepository.findByDoctor_Id(user.getId());

        } else if (user.getRole() == Role.PATIENT) {

            Patient patient = patientRepository.findByEmail(username)
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "Patient profile not found"
                    ));

            appointments =
                    appointmentRepository.findByPatient_Id(patient.getId());

        } else {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view appointments"
            );
        }

        return appointments.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Change appointment status
    @Transactional
    public AppointmentResponse updateAppointmentStatus(
            Long appointmentId,
            StatusRequest request,
            String username) {

        AppUser user = findUser(username);

        if (user.getRole() != Role.ADMIN &&
                user.getRole() != Role.DOCTOR) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors or administrators can update appointment status"
            );
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Appointment not found"
                ));

        // Doctors can only update their own appointments
        if (user.getRole() == Role.DOCTOR &&
                !appointment.getDoctor().getId().equals(user.getId())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You cannot modify another doctor's appointment"
            );
        }

        if (request.status() == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment status is required"
            );
        }

        appointment.setStatus(request.status());

        Appointment updatedAppointment =
                appointmentRepository.save(appointment);

        return mapToResponse(updatedAppointment);
    }

    // Find user by username
    private AppUser findUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Convert Appointment entity into response DTO
    private AppointmentResponse mapToResponse(Appointment appointment) {

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getFullName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getAppointmentDateTime(),
                appointment.getStatus()
        );
    }
}
