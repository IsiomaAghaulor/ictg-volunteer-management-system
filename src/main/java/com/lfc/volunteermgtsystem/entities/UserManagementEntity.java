package com.lfc.volunteermgtsystem.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CVM_USER_MGT")
@RequiredArgsConstructor
@Data
public class UserManagementEntity {
    @Id
    @SequenceGenerator(
            name = "CVM_USER_MGT_SEQ",
            sequenceName = "CVM_USER_MGT_SEQ",
            allocationSize = 1)
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "CVM_USER_MGT_SEQ")
    private Long id;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "PASSWORD", unique = true, nullable = false)
    private String password;

    @Column(name = "ROLE", unique = true, nullable = false)
    private String role;

    @Column(name = "USER_REGISTRATION_DATE", unique = true, nullable = false)
    private LocalDateTime userRegistrationDate;
}
