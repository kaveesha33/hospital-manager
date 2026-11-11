
package com.example.hospitalmanager.repository;

import com.example.hospitalmanager.model.MedicalRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatient_Id(Long patientId);

    boolean existsByPatient_Id(Long patientId);
}
