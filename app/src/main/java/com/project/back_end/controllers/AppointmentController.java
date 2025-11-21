package com.project.back_end.controllers;

import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    // 2. Autowire Dependencies:
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
        
        // Validate token for doctor role
        if (!Service.validateToken(token, "doctor")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            List<Appointment> appointments = appointmentService.getAppointment(date, patientName);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving appointments: " + e.getMessage());
        }
    }

    // 4. Define the `bookAppointment` Method:
    @PostMapping("/{token}")
    public ResponseEntity<?> bookAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        // Validate appointment data
        if (!service.validateAppointment(appointment)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid appointment data.");
        }
        
        try {
            Appointment bookedAppointment = appointmentService.bookAppointment(appointment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Appointment booked successfully with ID: " + bookedAppointment.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error booking appointment: " + e.getMessage());
        }
    }

    // 5. Define the `updateAppointment` Method:
    @PutMapping("/{token}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable String token,
            @RequestBody Appointment appointment) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            Appointment updatedAppointment = appointmentService.updateAppointment(appointment);
            return ResponseEntity.ok("Appointment updated successfully: " + updatedAppointment.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error updating appointment: " + e.getMessage());
        }
    }

    // 6. Define the `cancelAppointment` Method:
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            @PathVariable String token) {
        
        // Validate token for patient role
        if (!service.validateToken(token, "patient")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            boolean cancelled = appointmentService.cancelAppointment(id);
            if (cancelled) {
                return ResponseEntity.ok("Appointment cancelled successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Appointment not found or already cancelled.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error cancelling appointment: " + e.getMessage());
        }
    }
}
