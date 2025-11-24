package com.project.back_end.controllers;

import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private Service service;

    /**
     * Get patient details
     */
    @GetMapping
    public ResponseEntity<?> getPatient(@RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> result = patientService.getPatientDetails(cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient details: " + e.getMessage());
        }
    }

    /**
     * Create new patient (registration)
     */
    @PostMapping
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        
        try {
            boolean patientExists = !service.validatePatient(patient);
            if (patientExists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Patient with email or phone already exists");
            }
            
            int result = patientService.createPatient(patient);
            if (result == 1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Patient created successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create patient");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Patient login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        
        try {
            ResponseEntity<Map<String, String>> loginResult = service.validatePatientLogin(login);
            return loginResult;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + e.getMessage());
        }
    }

    /**
     * Get all patient appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getPatientAppointment(@RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> result = patientService.getPatientAppointment(null, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient appointments: " + e.getMessage());
        }
    }

    /**
     * Filter patient appointments by condition and doctor name
     */
    @GetMapping("/appointments/filter")
    public ResponseEntity<?> filterPatientAppointment(
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String name,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> filteredAppointments = service.filterPatient(condition, name, cleanToken);
            return filteredAppointments;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering patient appointments: " + e.getMessage());
        }
    }

    /**
     * Update patient details
     */
    @PutMapping
    public ResponseEntity<?> updatePatient(
            @RequestBody Patient patient,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, String>> result = patientService.updatePatient(null, patient, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating patient: " + e.getMessage());
        }
    }

    /**
     * Get upcoming appointments for patient
     */
    @GetMapping("/appointments/upcoming")
    public ResponseEntity<?> getUpcomingAppointments(@RequestHeader("Authorization") String token) {
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> result = patientService.getUpcomingAppointments(null, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving upcoming appointments: " + e.getMessage());
        }
    }

    /**
     * Filter appointments by condition only
     */
    @GetMapping("/appointments/filter/condition")
    public ResponseEntity<?> filterByCondition(
            @RequestParam String condition,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> result = service.filterPatient(condition, null, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering appointments by condition: " + e.getMessage());
        }
    }

    /**
     * Filter appointments by doctor name only
     */
    @GetMapping("/appointments/filter/doctor")
    public ResponseEntity<?> filterByDoctor(
            @RequestParam String name,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, Object>> result = service.filterPatient(null, name, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering appointments by doctor: " + e.getMessage());
        }
    }

    /**
     * Helper method to extract token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader; // Return as-is if no Bearer prefix
    }
}