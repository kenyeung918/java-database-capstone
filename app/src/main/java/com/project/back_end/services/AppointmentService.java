package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    // 2. Constructor Injection for Dependencies
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    private final Service validationService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            TokenService tokenService,
                            Service validationService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
        this.validationService = validationService;
    }

    // 4. Book Appointment Method
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            // Save the new appointment to the database
            Appointment savedAppointment = appointmentRepository.save(appointment);
            
            // If save operation fails, return 0; otherwise, return 1
            return savedAppointment != null ? 1 : 0;
            
        } catch (Exception e) {
            // Handle any exceptions and return appropriate result code
            System.err.println("Error booking appointment: " + e.getMessage());
            return 0;
        }
    }

    // 5. Update Appointment Method
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if appointment exists before updating
            Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(appointment.getId());
            
            if (!existingAppointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment existingAppointment = existingAppointmentOpt.get();
            
            // Validate whether the patient ID matches
            if (!existingAppointment.getPatient().getId().equals(appointment.getPatient().getId())) {
                response.put("error", "Patient ID mismatch - unauthorized to update this appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Check if the appointment is available for updating (not completed)
            if (existingAppointment.getStatus() == 1) { // Assuming 1 = Completed
                response.put("error", "Cannot update completed appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Ensure that the doctor is available at the specified time
            int validationResult = ((Object) validationService).validateAppointment(
                appointment.getDoctor().getId(), 
                appointment.getAppointmentTime()
            );
            
            if (validationResult != 1) {
                if (validationResult == -1) {
                    response.put("error", "Doctor not found");
                } else {
                    response.put("error", "Doctor not available at the specified time");
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Update appointment fields
            existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
            existingAppointment.setDoctor(appointment.getDoctor());
            existingAppointment.setStatus(appointment.getStatus());
            
            // Save the updated appointment
            appointmentRepository.save(existingAppointment);
            
            // Return success message
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Handle errors and return appropriate error message
            System.err.println("Error updating appointment: " + e.getMessage());
            response.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. Cancel Appointment Method
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find the appointment by ID
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            
            if (!appointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Extract patient information from token
            String patientEmail = tokenService.getUsernameFromToken(token);
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Ensure the patient who owns the appointment is trying to cancel it
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                response.put("error", "Unauthorized to cancel this appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Delete the appointment from the database
            appointmentRepository.delete(appointment);
            
            // Return success message
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Handle possible errors
            System.err.println("Error cancelling appointment: " + e.getMessage());
            response.put("error", "Failed to cancel appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 7. Get Appointments Method
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointments(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract doctor information from token
            String doctorEmail = tokenService.getUsernameFromToken(token);
            Doctor doctor = doctorRepository.findByEmail(doctorEmail);
            
            if (doctor == null) {
                response.put("error", "Doctor not found");
                return response;
            }
            
            // Calculate start and end of day for the given date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            List<Appointment> appointments;
            
            if (pname != null && !pname.trim().isEmpty()) {
                // Filter by patient name if provided
                appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctor.getId(), pname, startOfDay, endOfDay);
            } else {
                // Get all appointments for the doctor on the specific date
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctor.getId(), startOfDay, endOfDay);
            }
            
            // Return map containing the list of appointments
            response.put("appointments", appointments);
            response.put("count", appointments.size());
            response.put("date", date.toString());
            response.put("doctorId", doctor.getId());
            
        } catch (Exception e) {
            System.err.println("Error retrieving appointments: " + e.getMessage());
            response.put("error", "Failed to retrieve appointments: " + e.getMessage());
        }
        
        return response;
    }

    // 8. Change Status Method
    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(long appointmentId, int newStatus) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if appointment exists
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Update the status of the appointment
            appointmentRepository.updateStatus(newStatus, appointmentId);
            
            response.put("message", "Appointment status updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error changing appointment status: " + e.getMessage());
            response.put("error", "Failed to update appointment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Additional utility methods for enhanced functionality

    /**
     * Get appointment by ID
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAppointmentById(long id) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            if (appointmentOpt.isPresent()) {
                return ResponseEntity.ok(appointmentOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Appointment not found with ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving appointment: " + e.getMessage());
        }
    }

    /**
     * Get appointments for a specific patient
     */
    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Check appointment availability
     */
    @Transactional(readOnly = true)
    public boolean checkAppointmentAvailability(Long doctorId, LocalDateTime appointmentTime) {
        return !appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, appointmentTime);
    }

    /**
     * Get today's appointments for a doctor
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointments(Long doctorId) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, LocalDate.now());
    }
}
