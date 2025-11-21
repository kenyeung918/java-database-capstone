package com.project.back_end.controllers;

import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {

    // 2. Autowire Dependencies:
    @Autowired
    private PatientService patientService;
    
    @Autowired
    private Service service;

    // 3. Define the `getPatient` Method:
    @GetMapping("/{token}")
    public ResponseEntity<?> getPatient(@PathVariable String token) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // Extract patient identifier from token
            String patientIdentifier = service.extractIdentifier(token);
            Patient patient = patientService.getPatientDetails(patientIdentifier);
            
            if (patient != null) {
                return ResponseEntity.ok(patient);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Patient not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient details: " + e.getMessage());
        }
    }

    // 4. Define the `createPatient` Method:
    @PostMapping()
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        
        try {
            // Validate if patient already exists by email or phone number
            if (service.patientExists(patient.getEmail(), patient.getPhoneNumber())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Patient with email id or phone no already exist");
            }
            
            // Create new patient
            Patient createdPatient = patientService.createPatient(patient);
            if (createdPatient != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Signup successful");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internal server error");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    // 5. Define the `login` Method:
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        
        try {
            Map<String, Object> loginResult = service.validatePatientLogin(login);
            
            if (loginResult != null && (Boolean) loginResult.get("success")) {
                return ResponseEntity.ok(loginResult);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(loginResult != null ? loginResult.get("message") : "Login failed");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }

    // 6. Define the `getPatientAppointment` Method:
    @GetMapping("/appointments/{id}/{token}")
    public ResponseEntity<?> getPatientAppointment(
            @PathVariable Long id,
            @PathVariable String token) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            List<Map<String, Object>> appointments = patientService.getPatientAppointment(id);
            
            if (appointments != null) {
                return ResponseEntity.ok(Map.of("appointments", appointments));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No appointments found for patient");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient appointments: " + e.getMessage());
        }
    }

    // 7. Define the `filterPatientAppointment` Method:
    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<?> filterPatientAppointment(
            @PathVariable String condition,
            @PathVariable String name,
            @PathVariable String token) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // Extract patient identifier from token for additional security
            String patientIdentifier = service.extractIdentifier(token);
            
            Map<String, Object> filteredAppointments = service.filterPatient(condition, name, patientIdentifier);
            
            if (filteredAppointments != null && !filteredAppointments.isEmpty()) {
                return ResponseEntity.ok(filteredAppointments);
            } else {
                return ResponseEntity.ok(Map.of("message", "No appointments match the filter criteria"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering patient appointments: " + e.getMessage());
        }
    }
}


