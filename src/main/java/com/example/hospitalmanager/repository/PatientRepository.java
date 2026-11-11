
package com.example.hospitalmanager.repository;

import com.example.hospitalmanager.model.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByFullNameContainingIgnoreCase(String name);

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);
}
