package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // 1. Extend JpaRepository<Patient, Long> - Basic CRUD functionality inherited

    // 2. Custom Query Methods:

    /**
     * Find a patient by their email address
     * @param email the email to search for
     * @return Patient entity matching the email, or null if not found
     */
    Patient findByEmail(String email);

    /**
     * Find a patient by either their email or phone number
     * @param email the email to search for
     * @param phone the phone number to search for
     * @return Patient entity matching either the email or phone, or null if not found
     */
    Patient findByEmailOrPhone(String email, String phone);
}

