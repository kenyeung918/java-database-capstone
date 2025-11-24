package com.project.back_end.services;

import com.project.back_end.models.Patient;
import com.project.back_end.models.Appointment;
import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    // 2. Constructor Injection for Dependencies
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public PatientService(PatientRepository patientRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 3. createPatient Method
    @Transactional
    public int createPatient(Patient patient) {
        try {
            // Save the patient to the database
            Patient savedPatient = patientRepository.save(patient);
            
            // Returns 1 on success, and 0 on failure
            return savedPatient != null ? 1 : 0;
            
        } catch (Exception e) {
            // Log the error and return 0
            System.err.println("Error creating patient: " + e.getMessage());
            return 0;
        }
    }

    // 4. getPatientAppointment Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract email from token
            String patientEmail = tokenService.getUsernameFromToken(token);
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            // Check if patient exists and ID matches
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            if (!patient.getId().equals(id)) {
                response.put("error", "Unauthorized access to patient data");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Retrieve appointments for the patient
            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            
            // Convert appointments to AppointmentDTO objects
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching patient appointments: " + e.getMessage());
            response.put("error", "Failed to retrieve appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. filterByCondition Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Appointment> appointments;
            
            // Check the condition value (past or future)
            if ("past".equalsIgnoreCase(condition)) {
                // Status 1 for past appointments
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 1);
            } else if ("future".equalsIgnoreCase(condition)) {
                // Status 0 for future appointments
                appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 0);
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Convert appointments to DTOs
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            response.put("condition", condition);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error filtering appointments by condition: " + e.getMessage());
            response.put("error", "Failed to filter appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. filterByDoctor Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(String name, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Filter appointments by doctor name and patient ID
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(name, patientId);
            
            // Convert to DTOs
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            response.put("doctorName", name);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error filtering appointments by doctor: " + e.getMessage());
            response.put("error", "Failed to filter appointments by doctor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 7. filterByDoctorAndCondition Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String condition, String name, long patientId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Determine status based on condition
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1; // Completed appointments
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0; // Scheduled appointments
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Filter by doctor name, patient ID, and status
            List<Appointment> appointments = appointmentRepository
                .filterByDoctorNameAndPatientIdAndStatus(name, patientId, status);
            
            // Convert to DTOs
            List<AppointmentDTO> appointmentDTOs = appointments.stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            response.put("doctorName", name);
            response.put("condition", condition);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error filtering appointments by doctor and condition: " + e.getMessage());
            response.put("error", "Failed to filter appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 8. getPatientDetails Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract email from token
            String patientEmail = tokenService.getUsernameFromToken(token);
            
            // Fetch patient by email
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Return patient details (excluding sensitive information)
            Map<String, Object> patientDetails = new HashMap<>();
            patientDetails.put("id", patient.getId());
            patientDetails.put("name", patient.getName());
            patientDetails.put("email", patient.getEmail());
            patientDetails.put("phone", patient.getPhone());
            patientDetails.put("address", patient.getAddress());
            patientDetails.put("registrationDate", patient.getRegistrationDate());
            
            response.put("patient", patientDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching patient details: " + e.getMessage());
            response.put("error", "Failed to retrieve patient details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to convert Appointment to AppointmentDTO
    private AppointmentDTO convertToAppointmentDTO(Appointment appointment) {
        return new AppointmentDTO(
            appointment.getId(),
            appointment.getDoctor().getId(),
            appointment.getDoctor().getName(),
            appointment.getPatient().getId(),
            appointment.getPatient().getName(),
            appointment.getPatient().getEmail(),
            appointment.getPatient().getPhone(),
            appointment.getPatient().getAddress(),
            appointment.getAppointmentTime(),
            appointment.getStatus()
        );
    }

    // Additional utility methods

    /**
     * Get patient by ID
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPatientById(Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Patient patient = patientRepository.findById(id).orElse(null);
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Return patient without sensitive data
            Map<String, Object> patientInfo = new HashMap<>();
            patientInfo.put("id", patient.getId());
            patientInfo.put("name", patient.getName());
            patientInfo.put("email", patient.getEmail());
            patientInfo.put("phone", patient.getPhone());
            patientInfo.put("address", patient.getAddress());
            
            response.put("patient", patientInfo);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching patient by ID: " + e.getMessage());
            response.put("error", "Failed to retrieve patient");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update patient details
     */
    @Transactional
    public ResponseEntity<Map<String, String>> updatePatient(Long id, Patient updatedPatient, String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Verify patient ownership
            String patientEmail = tokenService.getUsernameFromToken(token);
            Patient currentPatient = patientRepository.findByEmail(patientEmail);
            
            if (currentPatient == null || !currentPatient.getId().equals(id)) {
                response.put("error", "Unauthorized to update this patient");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Update patient details
            currentPatient.setName(updatedPatient.getName());
            currentPatient.setPhone(updatedPatient.getPhone());
            currentPatient.setAddress(updatedPatient.getAddress());
            
            patientRepository.save(currentPatient);
            
            response.put("message", "Patient details updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error updating patient: " + e.getMessage());
            response.put("error", "Failed to update patient details");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get upcoming appointments for patient
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getUpcomingAppointments(Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify patient ownership
            String patientEmail = tokenService.getUsernameFromToken(token);
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            if (patient == null || !patient.getId().equals(patientId)) {
                response.put("error", "Unauthorized access");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            List<Appointment> upcomingAppointments = appointmentRepository
                .findUpcomingAppointmentsByPatientId(patientId, LocalDateTime.now());
            
            List<AppointmentDTO> appointmentDTOs = upcomingAppointments.stream()
                .map(this::convertToAppointmentDTO)
                .collect(Collectors.toList());
            
            response.put("appointments", appointmentDTOs);
            response.put("count", appointmentDTOs.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error fetching upcoming appointments: " + e.getMessage());
            response.put("error", "Failed to retrieve upcoming appointments");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}