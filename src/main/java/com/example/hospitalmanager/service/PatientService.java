
package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.PatientRequest;
import com.example.hospitalmanager.dto.PatientResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.repository.AppointmentRepository;
import com.example.hospitalmanager.repository.MedicalRecordRepository;
import com.example.hospitalmanager.repository.PatientRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public PatientService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            MedicalRecordRepository medicalRecordRepository) {

        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // Create a new patient
    @Transactional
    public PatientResponse createPatient(PatientRequest request) {

        if (patientRepository.existsByEmail(request.email())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Patient email is already registered"
            );
        }

        Patient patient = new Patient();

        patient.setFullName(request.fullName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setPhoneNumber(request.phoneNumber());
        patient.setEmail(request.email());
        patient.setAddress(request.address());

        Patient savedPatient = patientRepository.save(patient);

        return mapToResponse(savedPatient);
    }

    // Retrieve all patients
    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {

        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Retrieve one patient by ID
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {

        Patient patient = findPatient(id);

        return mapToResponse(patient);
    }

    // Search patients by name
    @Transactional(readOnly = true)
    public List<PatientResponse> searchPatients(String name) {

        return patientRepository
                .findByFullNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update patient details
    @Transactional
    public PatientResponse updatePatient(
            Long id,
            PatientRequest request) {

        Patient patient = findPatient(id);

        if (patientRepository.existsByEmail(request.email())
                && !patient.getEmail().equalsIgnoreCase(request.email())) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Email is already used by another patient"
            );
        }

        patient.setFullName(request.fullName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setPhoneNumber(request.phoneNumber());
        patient.setEmail(request.email());
        patient.setAddress(request.address());

        Patient updatedPatient = patientRepository.save(patient);

        return mapToResponse(updatedPatient);
    }

    // Delete a patient
    @Transactional
    public void deletePatient(Long id) {

        Patient patient = findPatient(id);

        boolean hasAppointments =
                appointmentRepository.existsByPatient_Id(id);

        boolean hasMedicalRecords =
                medicalRecordRepository.existsByPatient_Id(id);

        if (hasAppointments || hasMedicalRecords) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a patient who has appointments or medical records"
            );
        }

        patientRepository.delete(patient);
    }

    // Find patient or throw 404
    private Patient findPatient(Long id) {

        return patientRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));
    }

    // Convert Patient entity to response DTO
    private PatientResponse mapToResponse(Patient patient) {

        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhoneNumber(),
                patient.getEmail(),
                patient.getAddress()
        );
    }
}
