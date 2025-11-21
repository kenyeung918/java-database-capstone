package com.project.back_end.repo;


import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // 1. Extend JpaRepository<Doctor, Long> - Basic CRUD functionality inherited

    // 2. Custom Query Methods:

    /**
     * Find a Doctor by their email
     * @param email the email to search for
     * @return Doctor entity matching the email, or null if not found
     */
    Doctor findByEmail(String email);

    /**
     * Find doctors whose name contains the provided search string (case-sensitive)
     * Uses CONCAT('%', :name, '%') for partial matching
     * @param name the name pattern to search for
     * @return List of doctors with names containing the search string
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    /**
     * Find doctors where name contains search string (case-insensitive) 
     * and specialty matches exactly (case-insensitive)
     * @param name the name to search for (case-insensitive partial match)
     * @param specialty the specialty to match (case-insensitive exact match)
     * @return List of doctors matching both criteria
     */
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

    /**
     * Find doctors by specialty (case-insensitive)
     * @param specialty the specialty to search for
     * @return List of doctors with the specified specialty
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}