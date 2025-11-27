package com.project.back_end.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointment")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Doctor is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull(message = "Appointment time is required")
    @Future(message = "Appointment time must be in the future")
    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @NotNull(message = "Status is required")
    @Column(nullable = false)
    private Integer status; // 0 = SCHEDULED, 1 = COMPLETED, 2 = CANCELLED, 3 = NO_SHOW

     // Status constants for better code readability
    public static final int STATUS_SCHEDULED = 0;
    public static final int STATUS_COMPLETED = 1;
    public static final int STATUS_CANCELLED = 2;
    public static final int STATUS_NO_SHOW = 3;

     // Parameterized constructor
    public Appointment(Doctor doctor, Patient patient, LocalDateTime appointmentTime, String notes) { 
        this.doctor = doctor;
        this.patient = patient;
        this.appointmentTime = appointmentTime;        
    }

    // Helper method: Calculate end time (1 hour after start time)
    @Transient
    public LocalDateTime getEndTime() {
        if (appointmentTime != null) {
            return appointmentTime.plusHours(1);
        }
        return null;
    }

    // Helper method: Extract date portion only
    @Transient
    public LocalDate getAppointmentDate() {
        if (appointmentTime != null) {
            return appointmentTime.toLocalDate();
        }
        return null;
    }

    // Helper method: Extract time portion only
    @Transient
    public LocalTime getAppointmentTimeOnly() {
        if (appointmentTime != null) {
            return appointmentTime.toLocalTime();
        }
        return null;
    }

    // Helper method to get status as string
    @Transient
    public String getStatusString() {
        switch (status) {
            case STATUS_SCHEDULED:
                return "SCHEDULED";
            case STATUS_COMPLETED:
                return "COMPLETED";
            case STATUS_CANCELLED:
                return "CANCELLED";
            case STATUS_NO_SHOW:
                return "NO_SHOW";
            default:
                return "UNKNOWN";
        }
    }

    // Business logic methods
    @Transient
    public boolean isUpcoming() {
        return status == STATUS_SCHEDULED && appointmentTime != null && appointmentTime.isAfter(LocalDateTime.now());
    }

    @Transient
    public boolean canBeCancelled() {
        return status == STATUS_SCHEDULED && appointmentTime != null && 
               appointmentTime.isAfter(LocalDateTime.now().plusHours(2)); // Can cancel if more than 2 hours before
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // toString method
    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", doctor=" + (doctor != null ? doctor.getId() : "null") +
                ", patient=" + (patient != null ? patient.getId() : "null") +
                ", appointmentTime=" + appointmentTime +
                ", status=" + getStatusString() +
                '}';
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Appointment that = (Appointment) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}