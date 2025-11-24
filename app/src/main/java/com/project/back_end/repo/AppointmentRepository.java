package com.project.back_end.repo;
import com.project.back_end.models.Appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Custom Query Methods:

    /**
     * Retrieve appointments for a doctor within a given time range
     * Uses LEFT JOIN FETCH to include doctor and availability info
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.availableTimes " +
           "WHERE a.doctor.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    /**
     * Filter appointments by doctor ID, partial patient name (case-insensitive), and time range
     * Uses LEFT JOIN FETCH to include patient and doctor details
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor LEFT JOIN FETCH a.patient " +
           "WHERE a.doctor.id = :doctorId AND LOWER(a.patient.name) LIKE LOWER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("patientName") String patientName, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);

    /**
     * Delete all appointments associated with a particular doctor
     * Uses @Modifying and @Transactional for delete operation
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Find all appointments for a specific patient
     */
    List<Appointment> findByPatientId(Long patientId);

    /**
     * Retrieve appointments for a patient by status, ordered by appointment time
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * Search appointments by partial doctor name and patient ID
     * Uses LOWER and CONCAT for case-insensitive partial matching
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId);

    /**
     * Filter appointments by doctor name, patient ID, and status
     * Uses LOWER, CONCAT, and additional filtering on status
     */
    @Query("SELECT a FROM Appointment a WHERE LOWER(a.doctor.name) LIKE LOWER(CONCAT('%', :doctorName, '%')) " +
           "AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId, 
            @Param("status") int status);

    /**
     * Update the status of a specific appointment based on its ID
     * Uses @Modifying and @Transactional for update operation
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") Long id);

    // Additional useful methods

    /**
     * Find appointments by status
     */
    List<Appointment> findByStatus(int status);

    /**
     * Find appointments by doctor ID and status
     */
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, int status);

    /**
     * Find appointments by patient ID and doctor ID
     */
    List<Appointment> findByPatientIdAndDoctorId(Long patientId, Long doctorId);

    /**
     * Count appointments by status for a specific patient
     */
    long countByPatientIdAndStatus(Long patientId, int status);

    /**
     * Find upcoming appointments for a patient (after current time)
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentTime > :currentTime " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByPatientId(
            @Param("patientId") Long patientId, 
            @Param("currentTime") LocalDateTime currentTime);

    /**
     * Find appointments for a doctor on a specific date
     * Uses LocalDate instead of LocalDateTime for clearer date comparison
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND FUNCTION('DATE', a.appointmentTime) = :date")
    List<Appointment> findByDoctorIdAndAppointmentDate(
            @Param("doctorId") Long doctorId, 
            @Param("date") LocalDate date);

    /**
     * Check if an appointment exists for a doctor at a specific time
     */
    boolean existsByDoctorIdAndAppointmentTime(Long doctorId, LocalDateTime appointmentTime);

    /**
     * Find appointment by ID with fetched relationships
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor LEFT JOIN FETCH a.patient WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    /**
     * Find appointments with pagination support
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor LEFT JOIN FETCH a.patient " +
           "WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientIdWithDetails(@Param("patientId") Long patientId);

    /**
     * Find today's appointments for a doctor
     */
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND FUNCTION('DATE', a.appointmentTime) = CURRENT_DATE")
    List<Appointment> findTodayAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

    /**
     * Find appointments within a date range for a patient
     */
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentTime BETWEEN :startDate AND :endDate " +
           "ORDER BY a.appointmentTime ASC")
    List<Appointment> findByPatientIdAndDateRange(
            @Param("patientId") Long patientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    void deleteByDoctorId(long id);
}