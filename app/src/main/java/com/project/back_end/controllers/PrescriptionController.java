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
@RequestMapping("${api.path}" + "prescription")
public class PrescriptionController {

    // 2. Autowire Dependencies:
    @Autowired
    private PrescriptionService prescriptionService;
    
    @Autowired
    private Service service;

    // 3. Define the `savePrescription` Method:
    @PostMapping("/{token}")
    public ResponseEntity<?> savePrescription(
            @RequestBody Prescription prescription,
            @PathVariable String token) {
        
        // Validate token for doctor role
        if (!service.validateToken(token, "doctor")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // Save prescription using prescriptionService
            Prescription savedPrescription = prescriptionService.savePrescription(prescription);
            
            if (savedPrescription != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Prescription saved successfully with ID: " + savedPrescription.getId());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to save prescription");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving prescription: " + e.getMessage());
        }
    }

    // 4. Define the `getPrescription` Method:
    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<?> getPrescription(
            @PathVariable Long appointmentId,
            @PathVariable String token) {
        
        // Validate token for doctor role
        if (!service.validateToken(token, "doctor")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // Get prescription by appointment ID
            Prescription prescription = prescriptionService.getPrescription(appointmentId);
            
            if (prescription != null) {
                return ResponseEntity.ok(prescription);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No prescription found for appointment ID: " + appointmentId);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving prescription: " + e.getMessage());
        }
    }
}