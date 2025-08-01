package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.VolunteerAssignment;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import com.lfc.volunteermgtsystem.serviceImpls.VolunteerAssignmentService;
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
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VolunteerAssignmentController {
    
    @Autowired
    private VolunteerAssignmentService assignmentService;
    
    @GetMapping("/{id}")
    public ResponseEntity<VolunteerAssignment> getAssignmentById(@PathVariable Long id) {
        Optional<VolunteerAssignment> assignment = assignmentService.findById(id);
        return assignment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/volunteer/{volunteerId}")
    public ResponseEntity<Page<VolunteerAssignment>> getVolunteerAssignments(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<VolunteerAssignment> assignments = assignmentService.getVolunteerAssignments(volunteerId, pageable);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/my-assignments")
    public ResponseEntity<Page<VolunteerAssignment>> getMyAssignments(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<VolunteerAssignment> assignments = assignmentService.getVolunteerAssignments(currentUser.getId(), pageable);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<VolunteerAssignment>> getActivityAssignments(@PathVariable Long activityId) {
        List<VolunteerAssignment> assignments = assignmentService.getActivityAssignments(activityId);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<Page<VolunteerAssignment>> getAssignmentsByStatus(
            @PathVariable AssignmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("assignedAt").descending());
        Page<VolunteerAssignment> assignments = assignmentService.getAssignmentsByStatus(status, pageable);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<List<VolunteerAssignment>> getAssignmentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<VolunteerAssignment> assignments = assignmentService.getAssignmentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(assignments);
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<?> assignVolunteerToActivity(
            @RequestParam Long volunteerId,
            @RequestParam Long activityId,
            @RequestParam(required = false) Long activityRoleId,
            @AuthenticationPrincipal User currentUser) {
        try {
            VolunteerAssignment assignment = assignmentService.assignVolunteerToActivity(
                    volunteerId, activityId, currentUser.getId(), null);
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to assign volunteer");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/assign-substitute")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<?> assignSubstituteVolunteer(
            @RequestParam Long volunteerId,
            @RequestParam Long activityId,
            @AuthenticationPrincipal User currentUser) {
        try {
            VolunteerAssignment assignment = assignmentService.assignSubstituteVolunteer(
                    volunteerId, activityId, currentUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to assign substitute volunteer");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> volunteerSignUp(
            @RequestParam Long activityId,
            @AuthenticationPrincipal User currentUser) {
        try {
            assignmentService.volunteerSignUp(currentUser.getId(), activityId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully signed up for activity");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to sign up for activity");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAssignment(@PathVariable Long id) {
        try {
            assignmentService.confirmAssignment(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Assignment confirmed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to confirm assignment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAssignment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        try {
            assignmentService.cancelAssignment(id, reason);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Assignment cancelled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to cancel assignment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<?> completeAssignment(
            @PathVariable Long id,
            @RequestParam Double hoursServed,
            @RequestParam(required = false) String notes) {
        try {
            assignmentService.completeAssignment(id, hoursServed, notes);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Assignment completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to complete assignment");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/no-show")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<?> markNoShow(@PathVariable Long id) {
        try {
            assignmentService.markNoShow(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Assignment marked as no-show");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to mark assignment as no-show");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/volunteer/{volunteerId}/stats")
    public ResponseEntity<Map<String, Object>> getVolunteerStats(@PathVariable Long volunteerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHoursServed", assignmentService.getTotalHoursServedByVolunteer(volunteerId));
        stats.put("completedAssignments", assignmentService.getCompletedAssignmentCountByVolunteer(volunteerId));
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/conflict-check")
    public ResponseEntity<Map<String, Boolean>> checkConflicts(
            @RequestParam Long volunteerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        boolean hasConflicts = assignmentService.hasConflictingAssignments(volunteerId, startTime, endTime);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasConflicts", hasConflicts);
        return ResponseEntity.ok(response);
    }
}