package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;
    
    @Autowired
    private Service service;

    /**
     * Save prescription
     */
    @PostMapping
    public ResponseEntity<?> savePrescription(
            @RequestBody Prescription prescription,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header and validate properly
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: savePrescription returns ResponseEntity, not Prescription
            ResponseEntity<Map<String, String>> result = prescriptionService.savePrescription(prescription);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving prescription: " + e.getMessage());
        }
    }

    /**
     * Get prescription by appointment ID
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<?> getPrescription(
            @PathVariable Long appointmentId,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: getPrescription returns ResponseEntity, not Prescription
            ResponseEntity<Map<String, Object>> result = prescriptionService.getPrescription(appointmentId);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving prescription: " + e.getMessage());
        }
    }

    /**
     * Update prescription
     */
    @PutMapping("/{prescriptionId}")
    public ResponseEntity<?> updatePrescription(
            @PathVariable String prescriptionId,
            @RequestBody Prescription prescription,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: updatePrescription returns ResponseEntity
            ResponseEntity<Map<String, String>> result = prescriptionService.updatePrescription(prescriptionId, prescription);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating prescription: " + e.getMessage());
        }
    }

    /**
     * Delete prescription
     */
    @DeleteMapping("/{prescriptionId}")
    public ResponseEntity<?> deletePrescription(
            @PathVariable String prescriptionId,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: deletePrescription returns ResponseEntity
            ResponseEntity<Map<String, String>> result = prescriptionService.deletePrescription(prescriptionId);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting prescription: " + e.getMessage());
        }
    }

    /**
     * Get prescriptions by patient ID
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<?> getPrescriptionsByPatientId(
            @PathVariable String patientId,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: getPrescriptionsByPatientId returns ResponseEntity
            ResponseEntity<Map<String, Object>> result = prescriptionService.getPrescriptionsByPatientId(patientId);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving prescriptions by patient: " + e.getMessage());
        }
    }

    /**
     * Get prescriptions by doctor ID
     */
    @GetMapping("/doctor")
    public ResponseEntity<?> getPrescriptionsByDoctorId(
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: getPrescriptionsByDoctorId returns ResponseEntity
            ResponseEntity<Map<String, Object>> result = prescriptionService.getPrescriptionsByDoctorId(null);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving prescriptions by doctor: " + e.getMessage());
        }
    }

    /**
     * Search prescriptions by medication name
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchPrescriptionsByMedication(
            @RequestParam String medicationName,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: searchPrescriptionsByMedication returns ResponseEntity
            ResponseEntity<Map<String, Object>> result = prescriptionService.searchPrescriptionsByMedication(medicationName);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching prescriptions: " + e.getMessage());
        }
    }

    /**
     * Check if prescription exists for appointment
     */
    @GetMapping("/exists/{appointmentId}")
    public ResponseEntity<?> checkPrescriptionExists(
            @PathVariable Long appointmentId,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: checkPrescriptionExists returns ResponseEntity
            ResponseEntity<Map<String, Object>> result = prescriptionService.checkPrescriptionExists(appointmentId);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking prescription existence: " + e.getMessage());
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