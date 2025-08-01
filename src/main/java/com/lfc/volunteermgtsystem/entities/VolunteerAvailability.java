package com.lfc.volunteermgtsystem.entities;

import com.lfc.volunteermgtsystem.enums.AvailabilityType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "volunteer_availability")
@Data
public class VolunteerAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AvailabilityType type = AvailabilityType.RECURRING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "specific_date")
    private LocalDate specificDate;
    
    @Column(name = "unavailable_from")
    private LocalDateTime unavailableFrom;
    
    @Column(name = "unavailable_to")
    private LocalDateTime unavailableTo;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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