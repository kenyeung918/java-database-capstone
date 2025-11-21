package com.project.back_end.services;

import com.project.back_end.models.Doctor;
import com.project.back_end.DTO.Login;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    // 2. Constructor Injection for Dependencies
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                        AppointmentRepository appointmentRepository,
                        TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    // 4. getDoctorAvailability Method
    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, String date) {
        try {
            // Get the doctor
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            if (!doctorOpt.isPresent()) {
                return List.of(); // Return empty list if doctor not found
            }

            Doctor doctor = doctorOpt.get();
            List<String> allAvailableSlots = doctor.getAvailableTimes();

            // Get booked appointments for the doctor on the specified date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = string.plusDays(1).atStartOfDay();
            
            List<String> bookedSlots = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(appointment -> appointment.getAppointmentTime().toLocalTime().toString())
                .collect(Collectors.toList());

            // Filter out booked slots from available slots
            return allAvailableSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching doctor availability: " + e.getMessage());
            return List.of();
        }
    }

    // 5. saveDoctor Method
    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            // Check if doctor already exists by email
            Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
            if (existingDoctor != null) {
                return -1; // Doctor already exists
            }

            // Save the new doctor
            Doctor savedDoctor = doctorRepository.save(doctor);
            return savedDoctor != null ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Error saving doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    // 6. updateDoctor Method
    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            // Check if doctor exists by ID
            Optional<Doctor> existingDoctorOpt = doctorRepository.findById(doctor.getId());
            if (!existingDoctorOpt.isPresent()) {
                return -1; // Doctor not found
            }

            // Update and save the doctor
            Doctor updatedDoctor = doctorRepository.save(doctor);
            return updatedDoctor != null ? 1 : 0;

        } catch (Exception e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    // 7. getDoctors Method
    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        try {
            return doctorRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error fetching doctors: " + e.getMessage());
            return List.of();
        }
    }

    // 8. deleteDoctor Method
    @Transactional
    public int deleteDoctor(long id) {
        try {
            // Check if doctor exists
            Optional<Doctor> doctorOpt = doctorRepository.findById(id);
            if (!doctorOpt.isPresent()) {
                return -1; // Doctor not found
            }

            // Delete all appointments associated with the doctor
            appointmentRepository.deleteAllByDoctorId(id);

            // Delete the doctor
            doctorRepository.deleteById(id);

            return 1; // Success

        } catch (Exception e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            return 0; // Internal error
        }
    }

    // 9. validateDoctor Method
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();

        try {
            // Find doctor by email
            Doctor doctor = doctorRepository.findByEmail(login.getIdentifier());
            if (doctor == null) {
                response.put("error", "Doctor not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Verify password
            if (!doctor.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Generate token
            String token = tokenService.generateToken(doctor.getEmail(), "doctor");
            response.put("token", token);
            response.put("message", "Login successful");
            response.put("doctorId", String.valueOf(doctor.getId()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error validating doctor: " + e.getMessage());
            response.put("error", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 10. findDoctorByName Method
    @Transactional(readOnly = true)
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository.findByNameLike(name);
            response.put("doctors", doctors);
            response.put("count", doctors.size());

        } catch (Exception e) {
            System.err.println("Error finding doctors by name: " + e.getMessage());
            response.put("error", "Failed to search doctors");
        }

        return response;
    }

    // 11. filterDoctorsByNameSpecilityandTime Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by name and specialty
            List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            
            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
            
            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // 12. filterDoctorByTime Method (Private helper)
    private List<Doctor> filterDoctorByTime(List<Doctor> doctors, String amOrPm) {
        return doctors.stream()
            .filter(doctor -> {
                List<String> availableTimes = doctor.getAvailableTimes();
                if (availableTimes == null || availableTimes.isEmpty()) {
                    return false;
                }

                return availableTimes.stream()
                    .anyMatch(timeSlot -> isTimeInPeriod(timeSlot, amOrPm));
            })
            .collect(Collectors.toList());
    }

    // Helper method to check if time slot matches AM/PM period
    private boolean isTimeInPeriod(String timeSlot, String period) {
        try {
            // Parse time slot (assuming format like "09:00-10:00" or "09:00")
            String startTimeStr = timeSlot.split("-")[0].trim();
            LocalTime startTime = LocalTime.parse(startTimeStr);

            if ("AM".equalsIgnoreCase(period)) {
                return startTime.isBefore(LocalTime.NOON);
            } else if ("PM".equalsIgnoreCase(period)) {
                return !startTime.isBefore(LocalTime.NOON);
            }

            return false; // Invalid period
        } catch (Exception e) {
            return false;
        }
    }

    // 13. filterDoctorByNameAndTime Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndTime(String name, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by name
            List<Doctor> doctors = doctorRepository.findByNameLike(name);
            
            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
            
            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors by name and time: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // 14. filterDoctorByNameAndSpecility Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
            response.put("doctors", doctors);
            response.put("count", doctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors by name and specialty: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // 15. filterDoctorByTimeAndSpecility Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorByTimeAndSpecility(String specialty, String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // First filter by specialty
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            
            // Then filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(doctors, amOrPm);
            
            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors by time and specialty: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // 16. filterDoctorBySpecility Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
            response.put("doctors", doctors);
            response.put("count", doctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors by specialty: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // 17. filterDoctorsByTime Method
    @Transactional(readOnly = true)
    public Map<String, Object> filterDoctorsByTime(String amOrPm) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get all doctors
            List<Doctor> allDoctors = doctorRepository.findAll();
            
            // Filter by time
            List<Doctor> filteredDoctors = filterDoctorByTime(allDoctors, amOrPm);
            
            response.put("doctors", filteredDoctors);
            response.put("count", filteredDoctors.size());

        } catch (Exception e) {
            System.err.println("Error filtering doctors by time: " + e.getMessage());
            response.put("error", "Failed to filter doctors");
        }

        return response;
    }

    // Additional utility methods

    /**
     * Get doctor by ID
     */
    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    /**
     * Get doctor by email
     */
    @Transactional(readOnly = true)
    public Doctor getDoctorByEmail(String email) {
        return doctorRepository.findByEmail(email);
    }

    /**
     * Check if doctor exists by email
     */
    @Transactional(readOnly = true)
    public boolean doctorExists(String email) {
        return doctorRepository.findByEmail(email) != null;
    }

    /**
     * Get all specialties
     */
    @Transactional(readOnly = true)
    public List<String> getAllSpecialties() {
        return doctorRepository.findAll()
            .stream()
            .map(Doctor::getSpecialty)
            .distinct()
            .collect(Collectors.toList());
    }
}
