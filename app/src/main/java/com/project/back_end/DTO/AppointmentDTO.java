package com.project.back_end.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTO {
    
    // 1. 'id' field:
    private Long id;
    
    // 2. 'doctorId' field:
    private Long doctorId;
    
    // 3. 'doctorName' field:
    private String doctorName;
    
    // 4. 'patientId' field:
    private Long patientId;
    
    // 5. 'patientName' field:
    private String patientName;
    
    // 6. 'patientEmail' field:
    private String patientEmail;
    
    // 7. 'patientPhone' field:
    private String patientPhone;
    
    // 8. 'patientAddress' field:
    private String patientAddress;
    
    // 9. 'appointmentTime' field:
    private LocalDateTime appointmentTime;
    
    // 10. 'status' field:
    private int status;
    
    // 11. 'appointmentDate' field (Custom Getter):
    private LocalDate appointmentDate;
    
    // 12. 'appointmentTimeOnly' field (Custom Getter):
    private LocalTime appointmentTimeOnly;
    
    // 13. 'endTime' field (Custom Getter):
    private LocalDateTime endTime;

    // 14. Constructor:
    /**
     * Constructor that accepts all relevant fields and calculates derived fields
     */
    public AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId, 
                         String patientName, String patientEmail, String patientPhone, 
                         String patientAddress, LocalDateTime appointmentTime, int status) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhone = patientPhone;
        this.patientAddress = patientAddress;
        this.appointmentTime = appointmentTime;
        this.status = status;
        
        // Calculate derived fields based on appointmentTime
        if (appointmentTime != null) {
            this.appointmentDate = appointmentTime.toLocalDate();
            this.appointmentTimeOnly = appointmentTime.toLocalTime();
            this.endTime = appointmentTime.plusHours(1);
        }
    }

    // 15. Getters:

    public Long getId() {
        return id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientAddress() {
        return patientAddress;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public int getStatus() {
        return status;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public LocalTime getAppointmentTimeOnly() {
        return appointmentTimeOnly;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Optional: Add status helper methods for better readability
    public boolean isScheduled() {
        return status == 0;
    }

    public boolean isCompleted() {
        return status == 1;
    }

    public boolean isCancelled() {
        return status == 2;
    }

    public String getStatusText() {
        switch (status) {
            case 0: return "Scheduled";
            case 1: return "Completed";
            case 2: return "Cancelled";
            default: return "Unknown";
        }
    }
}
