package com.lfc.volunteermgtsystem.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ministries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ministry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_ministry_id")
    private Ministry parentMinistry;

    @OneToMany(mappedBy = "parentMinistry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Ministry> subMinistries = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ministry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserMinistry> userMinistries = new HashSet<>();

    @OneToMany(mappedBy = "ministry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Activity> activities = new HashSet<>();

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
