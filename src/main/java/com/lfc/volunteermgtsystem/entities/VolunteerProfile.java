package com.lfc.volunteermgtsystem.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CVM_VOLUNTEER_PROFILE")
@RequiredArgsConstructor
@Data
public class VolunteerProfile {
    @Id
    @SequenceGenerator(
            name = "CVM_VOLUNTEER_PROFILE_SEQ",
            sequenceName = "CVM_VOLUNTEER_PROFILE_SEQ",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "CVM_VOLUNTEER_PROFILE_SEQ")
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 500)
    @Column(name = "address")
    private String address;

    @Size(max = 100)
    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Size(max = 20)
    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Size(max = 255)
    @Column(name = "emergency_contact_relationship")
    private String emergencyContactRelationship;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "total_hours_served")
    private Integer totalHoursServed = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "volunteerProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VolunteerDocument> documents = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
