package com.lfc.volunteermgtsystem.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "volunteer_documents")
@Data
public class VolunteerDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_profile_id", nullable = false)
    private VolunteerProfile volunteerProfile;
    
    @NotBlank
    @Size(max = 255)
    @Column(name = "document_name", nullable = false)
    private String documentName;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "document_type", nullable = false)
    private String documentType;
    
    @NotBlank
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}