package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.entities.*;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import com.lfc.volunteermgtsystem.repositories.ActivityRepository;
import com.lfc.volunteermgtsystem.repositories.UserRepository;
import com.lfc.volunteermgtsystem.repositories.VolunteerAssignmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VolunteerAssignmentService {
    private VolunteerAssignmentRepository assignmentRepository;
    private UserRepository userRepository;
    private ActivityRepository activityRepository;
    private ActivityService activityService;
    private NotificationService notificationService;
    
    public VolunteerAssignment assignVolunteerToActivity(Long volunteerId, Long activityId,
                                                         Long assignedById, ActivityRole activityRole) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigner not found"));
        
        List<VolunteerAssignment> conflictingAssignments = assignmentRepository
                .findConflictingAssignments(volunteerId, activity.getStartTime(), activity.getEndTime());
        
        if (!conflictingAssignments.isEmpty()) {
            throw new RuntimeException("Volunteer has conflicting assignments during this time");
        }
        
        if (activity.getVolunteersAssigned() >= activity.getVolunteersNeeded()) {
            throw new RuntimeException("Activity is already fully staffed");
        }
        
        VolunteerAssignment assignment = new VolunteerAssignment();
        assignment.setVolunteer(volunteer);
        assignment.setActivity(activity);
        assignment.setActivityRole(activityRole);
        assignment.setAssignedBy(assignedBy);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        
        assignment = assignmentRepository.save(assignment);
        
        activityService.updateVolunteerCount(activityId);
        
        notificationService.sendAssignmentNotification(assignment);
        
        return assignment;
    }
    
    public VolunteerAssignment assignSubstituteVolunteer(Long volunteerId, Long activityId, Long assignedById) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigner not found"));
        
        VolunteerAssignment assignment = new VolunteerAssignment();
        assignment.setVolunteer(volunteer);
        assignment.setActivity(activity);
        assignment.setAssignedBy(assignedBy);
        assignment.setIsSubstitute(true);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        
        assignment = assignmentRepository.save(assignment);
        
        notificationService.sendAssignmentNotification(assignment);
        
        return assignment;
    }
    
    public void volunteerSignUp(Long volunteerId, Long activityId) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        List<VolunteerAssignment> conflictingAssignments = assignmentRepository
                .findConflictingAssignments(volunteerId, activity.getStartTime(), activity.getEndTime());
        
        if (!conflictingAssignments.isEmpty()) {
            throw new RuntimeException("You have conflicting assignments during this time");
        }
        
        if (activity.getVolunteersAssigned() >= activity.getVolunteersNeeded()) {
            throw new RuntimeException("Activity is already fully staffed");
        }
        
        VolunteerAssignment assignment = new VolunteerAssignment();
        assignment.setVolunteer(volunteer);
        assignment.setActivity(activity);
        assignment.setAssignedBy(volunteer);
        assignment.setStatus(AssignmentStatus.ASSIGNED);
        
        assignmentRepository.save(assignment);
        
        activityService.updateVolunteerCount(activityId);
        
        notificationService.sendSignUpConfirmationNotification(assignment);
    }
    
    public void confirmAssignment(Long assignmentId) {
        VolunteerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        assignment.setStatus(AssignmentStatus.CONFIRMED);
        assignment.setConfirmedAt(LocalDateTime.now());
        
        assignmentRepository.save(assignment);
        
        notificationService.sendConfirmationNotification(assignment);
    }
    
    public void cancelAssignment(Long assignmentId, String reason) {
        VolunteerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        assignment.setStatus(AssignmentStatus.CANCELLED);
        assignment.setNotes(reason);
        
        assignmentRepository.save(assignment);
        
        activityService.updateVolunteerCount(assignment.getActivity().getId());
        
        notificationService.sendCancellationNotification(assignment);
    }
    
    public void completeAssignment(Long assignmentId, Double hoursServed, String notes) {
        VolunteerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        assignment.setHoursServed(hoursServed);
        assignment.setNotes(notes);
        
        assignmentRepository.save(assignment);
        
        if (assignment.getVolunteer().getVolunteerProfile() != null) {
            VolunteerProfile profile = assignment.getVolunteer().getVolunteerProfile();
            profile.setTotalHoursServed(profile.getTotalHoursServed() + hoursServed.intValue());
        }
    }
    
    public void markNoShow(Long assignmentId) {
        VolunteerAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        assignment.setStatus(AssignmentStatus.NO_SHOW);
        assignmentRepository.save(assignment);
        
        activityService.updateVolunteerCount(assignment.getActivity().getId());
    }
    
    public Page<VolunteerAssignment> getVolunteerAssignments(Long volunteerId, Pageable pageable) {
        return assignmentRepository.findByVolunteerId(volunteerId, pageable);
    }
    
    public List<VolunteerAssignment> getActivityAssignments(Long activityId) {
        return assignmentRepository.findByActivityId(activityId);
    }
    
    public Page<VolunteerAssignment> getAssignmentsByStatus(AssignmentStatus status, Pageable pageable) {
        return assignmentRepository.findByStatus(status, pageable);
    }
    
    public Optional<VolunteerAssignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }
    
    public List<VolunteerAssignment> getAssignmentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return assignmentRepository.findByDateRange(startDate, endDate);
    }
    
    public Double getTotalHoursServedByVolunteer(Long volunteerId) {
        Double hours = assignmentRepository.getTotalHoursServedByVolunteer(volunteerId);
        return hours != null ? hours : 0.0;
    }
    
    public Long getCompletedAssignmentCountByVolunteer(Long volunteerId) {
        return assignmentRepository.getCompletedAssignmentCountByVolunteer(volunteerId);
    }
    
    public boolean hasConflictingAssignments(Long volunteerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<VolunteerAssignment> conflicts = assignmentRepository
                .findConflictingAssignments(volunteerId, startTime, endTime);
        return !conflicts.isEmpty();
    }
}