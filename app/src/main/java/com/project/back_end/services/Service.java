package com.project.back_end.services;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.DTO.Login;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Service {

    // 1. Declare Dependencies
    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    // 2. Constructor Injection
    @Autowired
    public Service(TokenService tokenService,
                  AdminRepository adminRepository,
                  DoctorRepository doctorRepository,
                  PatientRepository patientRepository,
                  DoctorService doctorService,
                  PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // 3. validateToken Method - Fixed to match requirements
    public Map<String, String> validateToken(String token, String user) {
        try {
            // Use tokenService.validateToken() with user type
            return tokenService.validateToken(token, user);
        } catch (Exception e) {
            return false;
        }
    }

    // 4. validateAdmin Method - Fixed return type and logic
    public ResponseEntity<Map<String, String>> validateAdmin(Admin receivedAdmin) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Use adminRepository.findByUsername() to check if admin exists
            Optional<Admin> adminOpt = adminRepository.findByUsername(receivedAdmin.getUsername());
            
            if (adminOpt.isEmpty()) {
                response.put("error", "Admin not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Admin admin = adminOpt.get();
            
            // Compare password
            if (!admin.getPassword().equals(receivedAdmin.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Generate token using tokenService
            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            response.put("message", "Admin authentication successful");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 5. filterDoctor Method - Fixed parameters and logic
    public Map<String, Object> filterDoctor(String name, String time, String speciality) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Use doctorService methods for filtering
            if (name != null && !name.isEmpty() && 
                time != null && !time.isEmpty() && 
                speciality != null && !speciality.isEmpty()) {
                // Filter by all three criteria
                response = doctorService.filterDoctorsByNameSpecilityandTime(name, speciality, time);
            } else if (name != null && !name.isEmpty() && speciality != null && !speciality.isEmpty()) {
                // Filter by name and specialty
                response = doctorService.filterDoctorByNameAndSpecility(name, speciality);
            } else if (name != null && !name.isEmpty() && time != null && !time.isEmpty()) {
                // Filter by name and time
                response = doctorService.filterDoctorByNameAndTime(name, time);
            } else if (speciality != null && !speciality.isEmpty() && time != null && !time.isEmpty()) {
                // Filter by specialty and time
                response = doctorService.filterDoctorByTimeAndSpecility(speciality, time);
            } else if (name != null && !name.isEmpty()) {
                // Filter by name only
                response = doctorService.findDoctorByName(name);
            } else if (speciality != null && !speciality.isEmpty()) {
                // Filter by specialty only
                response = doctorService.filterDoctorBySpecility(speciality);
            } else if (time != null && !time.isEmpty()) {
                // Filter by time only
                response = doctorService.filterDoctorsByTime(time);
            } else {
                // Return all doctors if no filters provided
                List<Doctor> allDoctors = doctorService.getDoctors();
                response.put("doctors", allDoctors);
                response.put("count", allDoctors.size());
                response.put("message", "All doctors retrieved");
            }
            
            return response;
            
        } catch (Exception e) {
            response.put("error", "Error filtering doctors: " + e.getMessage());
            return response;
        }
    }

    // 6. validateAppointment Method - Fixed to accept Appointment object
    public int validateAppointment(Appointment appointment) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(appointment.getDoctorId());
            if (doctorOpt.isEmpty()) {
                return -1; // Doctor doesn't exist
            }
            
            Doctor doctor = doctorOpt.get();
            
            // Get doctor availability for the appointment date
            LocalDate appointmentDate = appointment.getAppointmentDate().toLocalDate();
            Map<String, Object> availability = doctorService.getDoctorAvailability(doctor.getId(), appointmentDate.toString());
            
            // Extract available time slots
            @SuppressWarnings("unchecked")
            List<String> availableSlots = (List<String>) availability.get("availableSlots");
            
            if (availableSlots == null || availableSlots.isEmpty()) {
                return 0; // No available slots
            }
            
            // Check if requested time is available
            LocalTime requestedTime = appointment.getAppointmentDate().toLocalTime();
            String formattedRequestedTime = requestedTime.toString();
            
            for (String slot : availableSlots) {
                if (slot.contains(formattedRequestedTime)) {
                    return 1; // Time is available
                }
            }
            
            return 0; // Time is unavailable
            
        } catch (Exception e) {
            System.err.println("Error validating appointment: " + e.getMessage());
            return 0; // Return unavailable on error
        }
    }

    // 7. validatePatient Method - Fixed logic
    public boolean validatePatient(Patient patient) {
        try {
            // Check if patient exists by email or phone number
            // Assuming your repository has findByEmailOrPhoneNumber method
            Optional<Patient> existingByEmail = patientRepository.findByEmail(patient.getEmail());
            Optional<Patient> existingByPhone = patientRepository.findByPhoneNumber(patient.getPhoneNumber());
            
            // Return true if patient doesn't exist (valid for registration)
            // Return false if patient exists (invalid for registration)
            return existingByEmail.isEmpty() && existingByPhone.isEmpty();
            
        } catch (Exception e) {
            System.err.println("Error validating patient: " + e.getMessage());
            return false;
        }
    }

    // 8. validatePatientLogin Method - Fixed to match requirements
    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // Find patient by email
            Optional<Patient> patientOpt = patientRepository.findByEmail(login.getEmail());
            
            if (patientOpt.isEmpty()) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Patient patient = patientOpt.get();
            
            // Validate password
            if (!patient.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // Generate token
            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            response.put("message", "Patient login successful");
            response.put("patientId", String.valueOf(patient.getId()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 9. filterPatient Method - Fixed parameters and return type
    public Map<String, Object> filterPatient(String condition, String name, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Extract patient identifier from token
            String patientIdentifier = tokenService.extractIdentifier(token);
            
            // Use patientService methods for filtering
            if (condition != null && !condition.isEmpty() && name != null && !name.isEmpty()) {
                // Filter by both condition and name
                response = patientService.filterByConditionAndName(condition, name, patientIdentifier);
            } else if (condition != null && !condition.isEmpty()) {
                // Filter by condition only
                response = patientService.filterByCondition(condition, patientIdentifier);
            } else if (name != null && !name.isEmpty()) {
                // Filter by name only
                response = patientService.filterByName(name, patientIdentifier);
            } else {
                // Get all appointments if no filters
                response = patientService.getPatientAppointments(patientIdentifier);
            }
            
            return response;
            
        } catch (Exception e) {
            response.put("error", "Error filtering patient appointments: " + e.getMessage());
            return response;
        }
    }

    // Additional utility methods

    /**
     * Check if patient exists by email or phone number
     */
    public boolean patientExists(String email, String phoneNumber) {
        try {
            Optional<Patient> byEmail = patientRepository.findByEmail(email);
            Optional<Patient> byPhone = patientRepository.findByPhoneNumber(phoneNumber);
            return byEmail.isPresent() || byPhone.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract identifier from token
     */
    public String extractIdentifier(String token) {
        try {
            return tokenService.extractIdentifier(token);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract identifier from token", e);
        }
    }

    /**
     * Validate doctor login
     */
    public ResponseEntity<Map<String, String>> validateDoctorLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findByEmail(login.getEmail());
            
            if (doctorOpt.isEmpty()) {
                response.put("error", "Doctor not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            Doctor doctor = doctorOpt.get();
            
            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = tokenService.generateToken(doctor.getEmail());
            response.put("token", token);
            response.put("message", "Doctor login successful");
            response.put("doctorId", String.valueOf(doctor.getId()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}