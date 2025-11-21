package com.project.back_end.controllers;

import com.project.back_end.models.Doctor;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}" + "doctor")
public class DoctorController {

    // 2. Autowire Dependencies:
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private Service service;

    // 3. Define the `getDoctorAvailability` Method:
    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable String user,
            @PathVariable Long doctorId,
            @PathVariable String date,
            @PathVariable String token) {
        
        // Validate token for the specified user role
        if (!service.validateToken(token, user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            Map<String, Object> availability = doctorService.getDoctorAvailability(doctorId, date);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching doctor availability: " + e.getMessage());
        }
    }

    // 4. Define the `getDoctor` Method:
    @GetMapping
    public ResponseEntity<?> getDoctors() {
        try {
            List<Doctor> doctors = doctorService.getDoctors();
            return ResponseEntity.ok(Map.of("doctors", doctors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving doctors: " + e.getMessage());
        }
    }

    // 5. Define the `saveDoctor` Method:
    @PostMapping("/{token}")
    public ResponseEntity<?> saveDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        
        // Validate token for admin role
        if (!service.validateToken(token, "admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            Doctor savedDoctor = doctorService.saveDoctor(doctor);
            if (savedDoctor != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Doctor added to db");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Doctor already exists");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Some internal error occurred: " + e.getMessage());
        }
    }

    // 6. Define the `doctorLogin` Method:
    @PostMapping("/login")
    public ResponseEntity<?> doctorLogin(@RequestBody Login login) {
        try {
            Map<String, Object> loginResult = doctorService.validateDoctor(login);
            return ResponseEntity.ok(loginResult);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + e.getMessage());
        }
    }

    // 7. Define the `updateDoctor` Method:
    @PutMapping("/{token}")
    public ResponseEntity<?> updateDoctor(
            @RequestBody Doctor doctor,
            @PathVariable String token) {
        
        // Validate token for admin role
        if (!service.validateToken(token, "admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            int updatedDoctor = doctorService.updateDoctor(doctor);
            if (updatedDoctor != null) {
                return ResponseEntity.ok("Doctor updated");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Some internal error occurred: " + e.getMessage());
        }
    }

    // 8. Define the `deleteDoctor` Method:
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<?> deleteDoctor(
            @PathVariable Long id,
            @PathVariable String token) {
        
        // Validate token for admin role
        if (!service.validateToken(token, "admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            boolean deleted = doctorService.deleteDoctor(id);
            if (deleted) {
                return ResponseEntity.ok("Doctor deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor not found with id: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Some internal error occurred: " + e.getMessage());
        }
    }

    // 9. Define the `filter` Method:
    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<?> filterDoctors(
            @PathVariable String name,
            @PathVariable String time,
            @PathVariable String speciality) {
        
        try {
            Map<String, Object> filteredDoctors = service.filterDoctor(name, time, speciality);
            return ResponseEntity.ok(filteredDoctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering doctors: " + e.getMessage());
        }
    }
}
