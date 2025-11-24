package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private Service service;

    // 3. Define the `getAppointments` Method:
    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(
            @PathVariable String date,
            @PathVariable String patientName,
            @PathVariable String token) {
        
        // FIXED: Call instance method, not static method
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(token, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: Convert date string to LocalDate and use correct method signature
            LocalDate appointmentDate = LocalDate.parse(date);
            Map<String, Object> result = appointmentService.getAppointments(patientName, appointmentDate, token);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving appointments: " + e.getMessage());
        }
    }

    // 4. Define the `bookAppointment` Method:
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @RequestHeader("Authorization") String token,
            @RequestBody Appointment appointment) {
        
        // FIXED: Extract token from Authorization header and validate
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        // FIXED: validateAppointment returns int, not boolean
        int validationResult = service.validateAppointment(appointment);
        if (validationResult != 1) {
            String errorMessage = validationResult == -1 ? "Doctor not found" : "Appointment time not available";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid appointment data: " + errorMessage);
        }
        
        try {
            // FIXED: bookAppointment returns int, not Appointment
            int result = appointmentService.bookAppointment(appointment);
            if (result == 1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Appointment booked successfully");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Failed to book appointment");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error booking appointment: " + e.getMessage());
        }
    }

    // 5. Define the `updateAppointment` Method:
    @PutMapping("/update")
    public ResponseEntity<?> updateAppointment(
            @RequestHeader("Authorization") String token,
            @RequestBody Appointment appointment) {
        
        // FIXED: Extract token from Authorization header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: updateAppointment returns ResponseEntity, not Appointment
            ResponseEntity<Map<String, String>> result = appointmentService.updateAppointment(appointment);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating appointment: " + e.getMessage());
        }
    }

    // 6. Define the `cancelAppointment` Method:
    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from Authorization header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: cancelAppointment returns ResponseEntity, not boolean
            ResponseEntity<Map<String, String>> result = appointmentService.cancelAppointment(id, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling appointment: " + e.getMessage());
        }
    }

    // Additional endpoints

    @GetMapping("/patient/{token}")
    public ResponseEntity<?> getPatientAppointments(@RequestHeader("Authorization") String token) {
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // You might want to add this method to your Service class
            ResponseEntity<Map<String, Object>> result = service.filterPatient(null, null, cleanToken);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving patient appointments: " + e.getMessage());
        }
    }

    @PutMapping("/status/{appointmentId}/{newStatus}")
    public ResponseEntity<?> changeAppointmentStatus(
            @PathVariable Long appointmentId,
            @PathVariable int newStatus,
            @RequestHeader("Authorization") String token) {
        
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "doctor");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            ResponseEntity<Map<String, String>> result = appointmentService.changeStatus(appointmentId, newStatus);
            return result;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error changing appointment status: " + e.getMessage());
        }
    }

    // Helper method to extract token from Authorization header
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader; // Return as-is if no Bearer prefix
    }
}
