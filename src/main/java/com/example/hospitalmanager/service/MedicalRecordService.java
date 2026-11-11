
package com.example.hospitalmanager.service;

import com.example.hospitalmanager.dto.MedicalRecordRequest;
import com.example.hospitalmanager.dto.MedicalRecordResponse;
import com.example.hospitalmanager.exception.ApiException;
import com.example.hospitalmanager.model.AppUser;
import com.example.hospitalmanager.model.MedicalRecord;
import com.example.hospitalmanager.model.Patient;
import com.example.hospitalmanager.model.Role;
import com.example.hospitalmanager.repository.AppointmentRepository;
import com.example.hospitalmanager.repository.MedicalRecordRepository;
import com.example.hospitalmanager.repository.PatientRepository;
import com.example.hospitalmanager.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(
            MedicalRecordRepository medicalRecordRepository,
            PatientRepository patientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository) {

        this.medicalRecordRepository = medicalRecordRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // Create a medical record
    @Transactional
    public MedicalRecordResponse createMedicalRecord(
            String doctorUsername,
            MedicalRecordRequest request) {

        AppUser doctor = findUser(doctorUsername);

        if (doctor.getRole() != Role.DOCTOR) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only doctors can create medical records"
            );
        }

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));

        // Verify that the doctor has an appointment with this patient
        if (!hasDoctorAppointment(doctor.getId(), patient.getId())) {

            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You are not assigned to this patient"
            );
        }

        MedicalRecord medicalRecord = new MedicalRecord();

        medicalRecord.setPatient(patient);
        medicalRecord.setDoctor(doctor);
        medicalRecord.setDiagnosis(request.diagnosis());
        medicalRecord.setPrescription(request.prescription());

        MedicalRecord savedRecord =
                medicalRecordRepository.save(medicalRecord);

        return mapToResponse(savedRecord);
    }

    // Retrieve medical records for a patient
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getPatientRecords(
            Long patientId,
            String username) {

        AppUser user = findUser(username);

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Patient not found"
                ));

        // Administrators can view all patient records
        if (user.getRole() == Role.ADMIN) {
            return mapRecords(patientId);
        }

        // Doctors can view records of assigned patients
        if (user.getRole() == Role.DOCTOR) {

            if (!hasDoctorAppointment(user.getId(), patientId)) {

                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "You are not assigned to this patient"
                );
            }

            return mapRecords(patientId);
        }

        // Patients can view only their own medical records
        if (user.getRole() == Role.PATIENT) {

            if (!patient.getEmail().equalsIgnoreCase(username)) {

                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "You can only view your own medical records"
                );
            }

            return mapRecords(patientId);
        }

        throw new ApiException(
                HttpStatus.FORBIDDEN,
                "You cannot view medical records"
        );
    }

    // Retrieve and convert patient records
    private List<MedicalRecordResponse> mapRecords(Long patientId) {

        return medicalRecordRepository.findByPatient_Id(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Check whether a doctor has an appointment with a patient
    private boolean hasDoctorAppointment(
            Long doctorId,
            Long patientId) {

        return appointmentRepository.findByDoctor_Id(doctorId)
                .stream()
                .anyMatch(appointment ->
                        appointment.getPatient().getId().equals(patientId)
                );
    }

    // Find user by username
    private AppUser findUser(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Convert MedicalRecord entity to response DTO
    private MedicalRecordResponse mapToResponse(
            MedicalRecord record) {

        return new MedicalRecordResponse(
                record.getId(),
                record.getPatient().getId(),
                record.getPatient().getFullName(),
                record.getDoctor().getId(),
                record.getDoctor().getName(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getCreatedAt()
        );
    }
}
