package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.entities.Activity;
import com.lfc.volunteermgtsystem.entities.ActivityRole;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.enums.ActivityStatus;
import com.lfc.volunteermgtsystem.serviceImpls.ActivityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @GetMapping
    public ResponseEntity<Page<Activity>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Activity> activities = activityService.getAllActivities(pageable);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Activity> getActivityById(@PathVariable Long id) {
        Optional<Activity> activity = activityService.findById(id);
        return activity.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ministry/{ministryId}")
    public ResponseEntity<Page<Activity>> getActivitiesByMinistry(
            @PathVariable Long ministryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<Activity> activities = activityService.getActivitiesByMinistry(ministryId, pageable);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Activity>> getActivitiesByStatus(
            @PathVariable ActivityStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<Activity> activities = activityService.getActivitiesByStatus(status, pageable);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<List<Activity>> getUpcomingActivities() {
        List<Activity> activities = activityService.getUpcomingActivities();
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/need-volunteers")
    public ResponseEntity<List<Activity>> getActivitiesNeedingVolunteers() {
        List<Activity> activities = activityService.getActivitiesNeedingVolunteers();
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<Activity>> getActivitiesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        List<Activity> activities = activityService.getActivitiesByDateRange(startTime, endTime);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Activity>> searchActivities(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Activity> activities = activityService.searchActivities(q, pageable);
        return ResponseEntity.ok(activities);
    }
    
    @GetMapping("/my-activities")
    public ResponseEntity<Page<Activity>> getMyActivities(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<Activity> activities = activityService.getVolunteerActivities(currentUser.getId(), pageable);
        return ResponseEntity.ok(activities);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> createActivity(
            @Valid @RequestBody Activity activity,
            @RequestParam Long ministryId,
            @AuthenticationPrincipal User currentUser) {
        try {
            Activity createdActivity = activityService.createActivity(activity, ministryId, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdActivity);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> updateActivity(@PathVariable Long id, @Valid @RequestBody Activity activity) {
        try {
            Activity updatedActivity = activityService.updateActivity(id, activity);
            return ResponseEntity.ok(updatedActivity);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            activityService.deleteActivity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Activity deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> cancelActivity(@PathVariable Long id) {
        try {
            activityService.cancelActivity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Activity cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> completeActivity(@PathVariable Long id) {
        try {
            activityService.completeActivity(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Activity completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to complete activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> addActivityRole(@PathVariable Long id, @Valid @RequestBody ActivityRole role) {
        try {
            ActivityRole createdRole = activityService.addActivityRole(id, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add activity role");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}