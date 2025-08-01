package com.lfc.volunteermgtsystem.serviceImpls;
import com.lfc.volunteermgtsystem.entities.*;
import com.lfc.volunteermgtsystem.enums.ActivityStatus;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import com.lfc.volunteermgtsystem.repositories.ActivityRepository;
import com.lfc.volunteermgtsystem.repositories.MinistryRepository;
import com.lfc.volunteermgtsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActivityService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private MinistryRepository ministryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Activity createActivity(Activity activity, Long ministryId, Long createdById) {
        Ministry ministry = ministryRepository.findById(ministryId)
                .orElseThrow(() -> new RuntimeException("Ministry not found"));
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        activity.setMinistry(ministry);
        activity.setCreatedBy(createdBy);
        
        return activityRepository.save(activity);
    }
    
    public Optional<Activity> findById(Long id) {
        return activityRepository.findById(id);
    }
    
    public Page<Activity> getAllActivities(Pageable pageable) {
        return activityRepository.findAll(pageable);
    }
    
    public Page<Activity> getActivitiesByMinistry(Long ministryId, Pageable pageable) {
        return activityRepository.findByMinistryId(ministryId, pageable);
    }
    
    public Page<Activity> getActivitiesByStatus(ActivityStatus status, Pageable pageable) {
        return activityRepository.findByStatus(status, pageable);
    }
    
    public List<Activity> getActivitiesByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return activityRepository.findByDateRange(startTime, endTime);
    }
    
    public List<Activity> getUpcomingActivities() {
        return activityRepository.findUpcomingActivities(LocalDateTime.now());
    }
    
    public List<Activity> getActivitiesNeedingVolunteers() {
        return activityRepository.findActivitiesNeedingVolunteers();
    }
    
    public Page<Activity> searchActivities(String searchTerm, Pageable pageable) {
        return activityRepository.searchActivities(searchTerm, pageable);
    }
    
    public Page<Activity> getVolunteerActivities(Long volunteerId, Pageable pageable) {
        return activityRepository.findByVolunteerId(volunteerId, pageable);
    }
    
    public Activity updateActivity(Long id, Activity updatedActivity) {
        Activity existingActivity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        existingActivity.setName(updatedActivity.getName());
        existingActivity.setDescription(updatedActivity.getDescription());
        existingActivity.setStartTime(updatedActivity.getStartTime());
        existingActivity.setEndTime(updatedActivity.getEndTime());
        existingActivity.setLocation(updatedActivity.getLocation());
        existingActivity.setVolunteersNeeded(updatedActivity.getVolunteersNeeded());
        existingActivity.setRecurrenceType(updatedActivity.getRecurrenceType());
        existingActivity.setRecurrenceEndDate(updatedActivity.getRecurrenceEndDate());
        existingActivity.setSpecialRequirements(updatedActivity.getSpecialRequirements());
        existingActivity.setStatus(updatedActivity.getStatus());
        
        return activityRepository.save(existingActivity);
    }
    
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        activityRepository.delete(activity);
    }
    
    public void cancelActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        activity.setStatus(ActivityStatus.CANCELLED);
        activityRepository.save(activity);
    }
    
    public void completeActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        activity.setStatus(ActivityStatus.COMPLETED);
        activityRepository.save(activity);
    }
    
    public ActivityRole addActivityRole(Long activityId, ActivityRole role) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        role.setActivity(activity);
        activity.getActivityRoles().add(role);
        
        activityRepository.save(activity);
        return role;
    }
    
    public void updateVolunteerCount(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        int assignedCount = (int) activity.getAssignments().stream()
                .filter(assignment -> assignment.getStatus() != AssignmentStatus.CANCELLED)
                .count();
        
        activity.setVolunteersAssigned(assignedCount);
        activityRepository.save(activity);
    }
}