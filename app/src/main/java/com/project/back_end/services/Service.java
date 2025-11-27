package com.project.back_end.services;

import com.project.back_end.models.*;
import com.project.back_end.repo.*;
import org.springframework.http.ResponseEntity;
import com.project.back_end.DTO.Login; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {
    
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;

    // Constructor injection
    public Service(TokenService tokenService, 
                  AdminRepository adminRepository, 
                  DoctorRepository doctorRepository, 
                  PatientRepository patientRepository,
                  DoctorService doctorService,
                  PatientService patientService,
                  AppointmentService appointmentService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    /**
     * Validates the JWT token for a specific user
     */
    public ResponseEntity<Map<String, String>> validateToken(String token, String user) {
        Map<String, String> response = new HashMap<>();
        
        try {
            boolean isValid = tokenService.validateToken(token, user);
            if (!isValid) {
                response.put("error", "Invalid or expired token");
                return ResponseEntity.status(401).body(response);
            }
            
            response.put("message", "Token is valid");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Validates admin login credentials
     */
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // FIXED: adminRepository.findByUsername returns Admin directly, not Optional
            Admin admin = adminRepository.findByUsername(receivedAdmin.getUsername());
            
            if (admin == null) {
                response.put("error", "Admin not found");
                return ResponseEntity.status(401).body(response);
            }
            
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(401).body(response);
            }
            
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Filters doctors based on name, specialty, and available time
     */
    public Map<String, Object> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Doctor> doctors;
            
            if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorsByNameSpecialtyAndTime(name, specialty, time);
            } else if (name != null && !name.isEmpty() && specialty != null && !specialty.isEmpty()) {
                doctors = doctorService.filterDoctorsByNameAndSpecialty(name, specialty);
            } else if (name != null && !name.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorsByNameAndTime(name, time);
            } else if (specialty != null && !specialty.isEmpty() && time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorsBySpecialtyAndTime(specialty, time);
            } else if (name != null && !name.isEmpty()) {
                doctors = doctorService.filterDoctorsByName(name);
            } else if (specialty != null && !specialty.isEmpty()) {
                doctors = doctorService.filterDoctorsBySpecialty(specialty);
            } else if (time != null && !time.isEmpty()) {
                doctors = doctorService.filterDoctorsByTime(time);
            } else {
                doctors = doctorService.getAllDoctors();
            }
            
            response.put("doctors", doctors);
            response.put("count", doctors.size());
            response.put("status", "success");
            
        } catch (Exception e) {
            response.put("error", "Filtering failed: " + e.getMessage());
            response.put("status", "error");
            response.put("doctors", new ArrayList<>());
            response.put("count", 0);
        }
        
        return response;
    }

    /**
     * Validates appointment availability for a doctor
     */
    public int validateAppointment(Appointment appointment) {
        try {
            // Check if doctor exists - doctorRepository.findById returns Optional
            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctor().getId());
            
            if (doctorOpt.isEmpty()) {
                return -1; // Doctor doesn't exist
            }
            
            // Check if appointment time is available using AppointmentService
            boolean isAvailable = appointmentService.checkAppointmentAvailability(
                appointment.getDoctor().getId(), 
                appointment.getAppointmentTime()
            );
            
            return isAvailable ? 1 : 0; // 1 if available, 0 if not
            
        } catch (Exception e) {
            return -1; // Error occurred
        }
    }

    /**
     * Validates if patient already exists (for registration)
     */
    public boolean validatePatient(Patient patient) {
        try {
            // FIXED: patientRepository.findByEmailOrPhone returns Patient directly, not Optional
            Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
            return existingPatient == null; // true if patient doesn't exist, false if exists
            
        } catch (Exception e) {
            return false; // Error occurred, assume patient exists to prevent duplicates
        }
    }

    /**
     * Validates patient login credentials
     */
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
    Map<String, String> response = new HashMap<>();
    
    try {
        // FIXED: Change login.getEmail() to login.getIdentifier()
        Patient patient = patientRepository.findByEmail(login.getEmail()); // ← FIXED LINE

        if (patient == null) {
            response.put("error", "Patient not found");
            return ResponseEntity.status(401).body(response);
        }
        
        if (!patient.getPassword().equals(login.getPassword())) {
            response.put("error", "Invalid password");
            return ResponseEntity.status(401).body(response);
        }
        
        String token = tokenService.generateToken(patient.getEmail());
        response.put("token", token);
        response.put("message", "Login successful");
        response.put("patientId", patient.getId().toString());
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.put("error", "Login failed: " + e.getMessage());
        return ResponseEntity.status(500).body(response);
    }
}

    

    /**
     * Filters patient appointments based on condition and doctor name
     */
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract patient email from token
            String patientEmail = tokenService.extractIdentifier(token);
            
            // FIXED: patientRepository.findByEmail returns Patient directly, not Optional
            Patient patient = patientRepository.findByEmail(patientEmail);
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(404).body(response);
            }
            
            List<Appointment> appointments;
            
            if (condition != null && !condition.isEmpty() && name != null && !name.isEmpty()) {
                ResponseEntity<Map<String, Object>> result = patientService.filterByDoctorAndCondition(condition, name, patient.getId());
                appointments = extractAppointmentsFromResponse(result);
            } else if (condition != null && !condition.isEmpty()) {
                ResponseEntity<Map<String, Object>> result = patientService.filterByCondition(condition, patient.getId());
                appointments = extractAppointmentsFromResponse(result);
            } else if (name != null && !name.isEmpty()) {
                ResponseEntity<Map<String, Object>> result = patientService.filterByDoctor(name, patient.getId());
                appointments = extractAppointmentsFromResponse(result);
            } else {
                ResponseEntity<Map<String, Object>> result = patientService.getPatientAppointment(patient.getId(), token);
                appointments = extractAppointmentsFromResponse(result);
            }
            
            response.put("appointments", appointments);
            response.put("count", appointments.size());
            response.put("status", "success");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Filtering failed: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Helper method to extract appointments from ResponseEntity
     */
    @SuppressWarnings("unchecked")
    private List<Appointment> extractAppointmentsFromResponse(ResponseEntity<Map<String, Object>> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Object appointmentsObj = response.getBody().get("appointments");
            if (appointmentsObj instanceof List) {
                return (List<Appointment>) appointmentsObj;
            }
        }
        return new ArrayList<>();
    }

    // Additional utility methods

    /**
     * Get patient details by token
     */
    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        return patientService.getPatientDetails(token);
    }

    /**
     * Create new patient
     */
    public ResponseEntity<Map<String, String>> createPatient(Patient patient) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // First validate if patient already exists
            if (!validatePatient(patient)) {
                response.put("error", "Patient already exists with this email or phone");
                return ResponseEntity.status(400).body(response);
            }
            
            int result = patientService.createPatient(patient);
            if (result == 1) {
                response.put("message", "Patient created successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to create patient");
                return ResponseEntity.status(500).body(response);
            }
        } catch (Exception e) {
            response.put("error", "Failed to create patient: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}