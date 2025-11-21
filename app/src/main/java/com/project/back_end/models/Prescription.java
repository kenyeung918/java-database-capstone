package com.project.back_end.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Document(collection = "prescriptions")
public class Prescription {
    
    @Id
    private String id;

    @NotNull(message = "Patient name is required")
    @Size(min = 3, max = 100, message = "Patient name must be between 3 and 100 characters")
    @Field("patient_name")
    private String patientName;

    @NotNull(message = "Appointment ID is required")
    @Field("appointment_id")
    private Long appointmentId;

    @NotNull(message = "Medication name is required")
    @Size(min = 3, max = 100, message = "Medication name must be between 3 and 100 characters")
    @Field("medication")
    private String medication;

    @NotNull(message = "Dosage is required")
    @Size(min = 3, max = 20, message = "Dosage must be between 3 and 20 characters")
    @Field("dosage")
    private String dosage;

    @Size(max = 200, message = "Doctor notes cannot exceed 200 characters")
    @Field("doctor_notes")
    private String doctorNotes;

    @Field("frequency")
    private String frequency; // e.g., "Once daily", "Twice daily"

    @Field("duration")
    private String duration; // e.g., "7 days", "30 days"

    @Field("quantity")
    private String quantity; // e.g., "30 tablets", "100ml"

    @Field("refills")
    private Integer refills = 0;

    @Field("is_active")
    private Boolean isActive = true;

    @Field("prescribed_date")
    private LocalDateTime prescribedDate;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    // Default constructor
    public Prescription() {
        this.prescribedDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with required fields
    public Prescription(String patientName, Long appointmentId, String medication, String dosage) {
        this();
        this.patientName = patientName;
        this.appointmentId = appointmentId;
        this.medication = medication;
        this.dosage = dosage;
    }

    // Constructor with all fields including doctor notes
    public Prescription(String patientName, Long appointmentId, String medication, String dosage, String doctorNotes) {
        this(patientName, appointmentId, medication, dosage);
        this.doctorNotes = doctorNotes;
    }

    // Business logic methods
    public boolean hasRefills() {
        return refills != null && refills > 0;
    }

    public boolean isExpired() {
        // Assuming prescriptions expire after 1 year
        return prescribedDate != null && 
               prescribedDate.plusYears(1).isBefore(LocalDateTime.now());
    }

    public String getPrescriptionSummary() {
        return String.format("%s - %s (%s)", medication, dosage, frequency != null ? frequency : "As directed");
    }

    // PrePersist method for MongoDB (simulated)
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (prescribedDate == null) {
            prescribedDate = LocalDateTime.now();
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public Integer getRefills() {
        return refills;
    }

    public void setRefills(Integer refills) {
        this.refills = refills;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getPrescribedDate() {
        return prescribedDate;
    }

    public void setPrescribedDate(LocalDateTime prescribedDate) {
        this.prescribedDate = prescribedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "id='" + id + '\'' +
                ", patientName='" + patientName + '\'' +
                ", appointmentId=" + appointmentId +
                ", medication='" + medication + '\'' +
                ", dosage='" + dosage + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}