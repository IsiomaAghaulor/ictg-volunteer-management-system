package com.lfc.volunteermgtsystem.entities;

import com.lfc.volunteermgtsystem.enums.NotificationChannel;
import com.lfc.volunteermgtsystem.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;
    
    @NotBlank
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "NOTIFICATION_TYPE")
    private NotificationType type = NotificationType.INFO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "NOTIFICATION_CHANNEL")
    private NotificationChannel channel = NotificationChannel.EMAIL;
    
    @Column(name = "read_status")
    private Boolean readStatus = false;
    
    @Column(name = "sent_status")
    private Boolean sentStatus = false;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType;
    
    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}