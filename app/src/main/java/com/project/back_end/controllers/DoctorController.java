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

    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private Service service;

    // 3. Define the `getDoctorAvailability` Method:
    @GetMapping("/availability/{doctorId}/{date}")
    public ResponseEntity<?> getDoctorAvailability(
            @PathVariable Long doctorId,
            @PathVariable String date,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header and validate properly
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "patient");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: getDoctorAvailability returns List<String>, not Map<String, Object>
            List<String> availability = doctorService.getDoctorAvailability(doctorId, date);
            return ResponseEntity.ok(Map.of(
                "availability", availability,
                "doctorId", doctorId,
                "date", date
            ));
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
            return ResponseEntity.ok(Map.of("doctors", doctors, "count", doctors.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving doctors: " + e.getMessage());
        }
    }

    // 5. Define the `saveDoctor` Method:
    @PostMapping
    public ResponseEntity<?> saveDoctor(
            @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: saveDoctor returns int, not Doctor
            int result = doctorService.saveDoctor(doctor);
            if (result == 1) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Doctor added to database successfully");
            } else if (result == -1) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Doctor already exists with this email");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to save doctor");
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
            // FIXED: validateDoctor returns ResponseEntity, not Map
            ResponseEntity<Map<String, String>> loginResult = doctorService.validateDoctor(login);
            return loginResult;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Login failed: " + e.getMessage());
        }
    }

    // 7. Define the `updateDoctor` Method:
    @PutMapping
    public ResponseEntity<?> updateDoctor(
            @RequestBody Doctor doctor,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: updateDoctor returns int, not Doctor
            int result = doctorService.updateDoctor(doctor);
            if (result == 1) {
                return ResponseEntity.ok("Doctor updated successfully");
            } else if (result == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor not found");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update doctor");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Some internal error occurred: " + e.getMessage());
        }
    }

    // 8. Define the `deleteDoctor` Method:
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        
        // FIXED: Extract token from header
        String cleanToken = extractTokenFromHeader(token);
        ResponseEntity<Map<String, String>> tokenValidation = service.validateToken(cleanToken, "admin");
        if (tokenValidation.getStatusCode() != HttpStatus.OK) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token. Access denied.");
        }
        
        try {
            // FIXED: deleteDoctor returns int, not boolean
            int result = doctorService.deleteDoctor(id);
            if (result == 1) {
                return ResponseEntity.ok("Doctor deleted successfully");
            } else if (result == -1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor not found with id: " + id);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete doctor");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Some internal error occurred: " + e.getMessage());
        }
    }

    // 9. Define the `filter` Method:
    @GetMapping("/filter")
    public ResponseEntity<?> filterDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String specialty) {
        
        try {
            // FIXED: Parameter order - name, specialty, time
            Map<String, Object> filteredDoctors = service.filterDoctor(name, specialty, time);
            return ResponseEntity.ok(filteredDoctors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error filtering doctors: " + e.getMessage());
        }
    }

    // Additional endpoints

    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        try {
            // FIXED: getDoctorById returns Optional<Doctor>
            java.util.Optional<Doctor> doctorOpt = doctorService.getDoctorById(id);
            if (doctorOpt.isPresent()) {
                return ResponseEntity.ok(doctorOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Doctor not found with id: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving doctor: " + e.getMessage());
        }
    }

    @GetMapping("/specialties")
    public ResponseEntity<?> getAllSpecialties() {
        try {
            List<String> specialties = doctorService.getAllSpecialties();
            return ResponseEntity.ok(Map.of("specialties", specialties));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving specialties: " + e.getMessage());
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
