package com.project.back_end.repo;

import com.project.back_end.models.Prescription;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    // CORRECT methods based on your Prescription entity:

    // 1. Find by appointment ID - This is correct
    List<Prescription> findByAppointmentId(Long appointmentId);

    // 2. FIXED: Search by medication (String field)
    List<Prescription> findByMedicationContainingIgnoreCase(String medication);

    // 3. FIXED: Search by patient name (String field)
    List<Prescription> findByPatientNameContainingIgnoreCase(String patientName);

    // 4. Check if prescription exists for appointment - This is correct
    boolean existsByAppointmentId(Long appointmentId);

    // Additional useful methods based on your entity:
    List<Prescription> findByIsActiveTrue();
    List<Prescription> findByPatientName(String patientName);
}