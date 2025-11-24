package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final TokenService tokenService;
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                            PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            TokenService tokenService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.tokenService = tokenService;
    }

    // 4. Book Appointment Method
    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            // FIXED: Use the correct validation method
            boolean isAvailable = checkAppointmentAvailability(
                appointment.getDoctor().getId(), 
                appointment.getAppointmentTime()
            );
            
            if (!isAvailable) {
                return 0; // Time slot not available
            }
            
            // Set default status if not provided
            if (appointment.getStatus() == null) {
                appointment.setStatus(Appointment.STATUS_SCHEDULED);
            }
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            return savedAppointment != null ? 1 : 0;
            
        } catch (Exception e) {
            System.err.println("Error booking appointment: " + e.getMessage());
            return -1;
        }
    }

    // 5. Update Appointment Method
    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(appointment.getId());
            
            if (!existingAppointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment existingAppointment = existingAppointmentOpt.get();
            
            if (!existingAppointment.getPatient().getId().equals(appointment.getPatient().getId())) {
                response.put("error", "Patient ID mismatch - unauthorized to update this appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (existingAppointment.getStatus() == Appointment.STATUS_COMPLETED) {
                response.put("error", "Cannot update completed appointment");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Check if the new time slot is available (if time is being changed)
            if (!existingAppointment.getAppointmentTime().equals(appointment.getAppointmentTime())) {
                boolean isAvailable = checkAppointmentAvailability(
                    appointment.getDoctor().getId(), 
                    appointment.getAppointmentTime()
                );
                
                if (!isAvailable) {
                    response.put("error", "Doctor not available at the specified time");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            
            existingAppointment.setAppointmentTime(appointment.getAppointmentTime());
            existingAppointment.setDoctor(appointment.getDoctor());
            existingAppointment.setStatus(appointment.getStatus());
            existingAppointment.setNotes(appointment.getNotes());
            
            appointmentRepository.save(existingAppointment);
            
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            response.put("error", "Failed to update appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 6. Cancel Appointment Method
    @Transactional
    public ResponseEntity<Map<String, String>> cancelAppointment(long id, String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            
            if (!appointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            String patientEmail = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(patientEmail);
            
            if (patient == null) {
                response.put("error", "Patient not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (!appointment.getPatient().getId().equals(patient.getId())) {
                response.put("error", "Unauthorized to cancel this appointment");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            if (!appointment.canBeCancelled()) {
                response.put("error", "Appointment cannot be cancelled (less than 2 hours before)");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            appointment.setStatus(Appointment.STATUS_CANCELLED);
            appointmentRepository.save(appointment);
            
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            response.put("error", "Failed to cancel appointment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 7. Get Appointments Method
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointments(String pname, LocalDate date, String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String doctorEmail = tokenService.extractIdentifier(token);
            Doctor doctor = doctorRepository.findByEmail(doctorEmail);
            
            if (doctor == null) {
                response.put("error", "Doctor not found");
                return response;
            }
            
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            
            List<Appointment> appointments;
            
            if (pname != null && !pname.trim().isEmpty()) {
                appointments = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                    doctor.getId(), pname, startOfDay, endOfDay);
            } else {
                appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                    doctor.getId(), startOfDay, endOfDay);
            }
            
            response.put("appointments", appointments);
            response.put("count", appointments.size());
            response.put("date", date.toString());
            response.put("doctorId", doctor.getId());
            
        } catch (Exception e) {
            System.err.println("Error retrieving appointments: " + e.getMessage());
            response.put("error", "Failed to retrieve appointments: " + e.getMessage());
        }
        
        return response;
    }

    // 8. Change Status Method
    @Transactional
    public ResponseEntity<Map<String, String>> changeStatus(long appointmentId, int newStatus) {
        Map<String, String> response = new HashMap<>();
        
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            if (!appointmentOpt.isPresent()) {
                response.put("error", "Appointment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            Appointment appointment = appointmentOpt.get();
            
            if (!isValidStatusTransition(appointment.getStatus(), newStatus)) {
                response.put("error", "Invalid status transition");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            appointment.setStatus(newStatus);
            appointmentRepository.save(appointment);
            
            response.put("message", "Appointment status updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error changing appointment status: " + e.getMessage());
            response.put("error", "Failed to update appointment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to validate appointment availability
    public boolean checkAppointmentAvailability(Long doctorId, LocalDateTime appointmentTime) {
        try {
            Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
            
            if (doctorOpt.isEmpty()) {
                return false;
            }
            
            return !appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, appointmentTime);
            
        } catch (Exception e) {
            return false;
        }
    }

    // Helper method to validate status transitions
    private boolean isValidStatusTransition(int currentStatus, int newStatus) {
        switch (currentStatus) {
            case Appointment.STATUS_SCHEDULED:
                return newStatus == Appointment.STATUS_COMPLETED || 
                       newStatus == Appointment.STATUS_CANCELLED || 
                       newStatus == Appointment.STATUS_NO_SHOW;
            case Appointment.STATUS_COMPLETED:
                return false;
            case Appointment.STATUS_CANCELLED:
                return false;
            case Appointment.STATUS_NO_SHOW:
                return false;
            default:
                return false;
        }
    }

    // Additional utility methods
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAppointmentById(long id) {
        try {
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
            if (appointmentOpt.isPresent()) {
                return ResponseEntity.ok(appointmentOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Appointment not found with ID: " + id);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving appointment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointments(Long doctorId) {
        return appointmentRepository.findTodayAppointmentsByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsByPatientId(Long patientId) {
        return appointmentRepository.findUpcomingAppointmentsByPatientId(patientId, LocalDateTime.now());
    }

    @Transactional
    public ResponseEntity<Map<String, String>> completeAppointment(long appointmentId) {
        return changeStatus(appointmentId, Appointment.STATUS_COMPLETED);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> markAsNoShow(long appointmentId) {
        return changeStatus(appointmentId, Appointment.STATUS_NO_SHOW);
    }
}