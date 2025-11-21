package com.project.back_end.services;

import com.project.back_end.models.Prescription;
import com.project.back_end.repo.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrescriptionService {
    
    // 2. Constructor Injection for Dependencies
    private final PrescriptionRepository prescriptionRepository;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    // 3. savePrescription Method
    public ResponseEntity<Map<String, String>> savePrescription(Prescription prescription) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if a prescription already exists for the same appointment
            List<Prescription> existingPrescriptions = prescriptionRepository.findByAppointmentId(prescription.getAppointmentId());
            
            if (!existingPrescriptions.isEmpty()) {
                response.put("error", "Prescription already exists for this appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Save the new prescription to the database
            Prescription savedPrescription = prescriptionRepository.save(prescription);
            
            if (savedPrescription != null) {
                response.put("message", "Prescription saved successfully");
                response.put("prescriptionId", savedPrescription.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                response.put("error", "Failed to save prescription");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            // Log the error and return 500 Internal Server Error
            System.err.println("Error saving prescription: " + e.getMessage());
            response.put("error", "Internal server error while saving prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 4. getPrescription Method
    public ResponseEntity<Map<String, Object>> getPrescription(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Retrieve the prescription associated with the specific appointment ID
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            
            if (prescriptions.isEmpty()) {
                response.put("message", "No prescription found for this appointment");
                response.put("count", 0);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Return the prescription details
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("appointmentId", appointmentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // Log the error and return 500 Internal Server Error
            System.err.println("Error retrieving prescription: " + e.getMessage());
            response.put("error", "Internal server error while retrieving prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Additional utility methods for enhanced functionality

    /**
     * Update an existing prescription
     */
    public ResponseEntity<Map<String, String>> updatePrescription(String prescriptionId, Prescription updatedPrescription) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if prescription exists
            if (!prescriptionRepository.existsById(prescriptionId)) {
                response.put("error", "Prescription not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Set the ID and save
            updatedPrescription.setId(prescriptionId);
            Prescription savedPrescription = prescriptionRepository.save(updatedPrescription);
            
            if (savedPrescription != null) {
                response.put("message", "Prescription updated successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to update prescription");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error updating prescription: " + e.getMessage());
            response.put("error", "Internal server error while updating prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a prescription by ID
     */
    public ResponseEntity<Map<String, String>> deletePrescription(String prescriptionId) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Check if prescription exists
            if (!prescriptionRepository.existsById(prescriptionId)) {
                response.put("error", "Prescription not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            // Delete the prescription
            prescriptionRepository.deleteById(prescriptionId);
            
            response.put("message", "Prescription deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error deleting prescription: " + e.getMessage());
            response.put("error", "Internal server error while deleting prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get prescriptions by patient ID
     */
    public ResponseEntity<Map<String, Object>> getPrescriptionsByPatientId(String patientId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByPatientId(patientId);
            
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("patientId", patientId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error retrieving prescriptions by patient: " + e.getMessage());
            response.put("error", "Internal server error while retrieving prescriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get prescriptions by doctor ID
     */
    public ResponseEntity<Map<String, Object>> getPrescriptionsByDoctorId(String doctorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByDoctorId(doctorId);
            
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("doctorId", doctorId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error retrieving prescriptions by doctor: " + e.getMessage());
            response.put("error", "Internal server error while retrieving prescriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Search prescriptions by medication name
     */
    public ResponseEntity<Map<String, Object>> searchPrescriptionsByMedication(String medicationName) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByMedicationNameContainingIgnoreCase(medicationName);
            
            response.put("prescriptions", prescriptions);
            response.put("count", prescriptions.size());
            response.put("medicationName", medicationName);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error searching prescriptions by medication: " + e.getMessage());
            response.put("error", "Internal server error while searching prescriptions");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check if prescription exists for appointment
     */
    public ResponseEntity<Map<String, Object>> checkPrescriptionExists(Long appointmentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean exists = prescriptionRepository.existsByAppointmentId(appointmentId);
            
            response.put("exists", exists);
            response.put("appointmentId", appointmentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error checking prescription existence: " + e.getMessage());
            response.put("error", "Internal server error while checking prescription");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
