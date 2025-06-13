package com.lfc.volunteermgtsystem.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

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

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserManagementEntity user;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "PHONE")
    private String phoneNumber;

    private String address;
    private LocalDate dob;
    private String availability;
    private String skills;
    private LocalDate joinedDate;
}
