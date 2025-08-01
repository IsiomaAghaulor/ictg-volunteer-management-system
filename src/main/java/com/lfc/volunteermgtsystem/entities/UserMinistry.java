package com.lfc.volunteermgtsystem.entities;

import com.lfc.volunteermgtsystem.enums.MinistryRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_ministries")
@Data
public class UserMinistry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ministry_id", nullable = false)
    private Ministry ministry;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private MinistryRole role = MinistryRole.VOLUNTEER;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }
}