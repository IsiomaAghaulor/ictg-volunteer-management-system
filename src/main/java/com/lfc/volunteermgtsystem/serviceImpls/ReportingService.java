package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.entities.Activity;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.VolunteerAssignment;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import com.lfc.volunteermgtsystem.enums.Role;
import com.lfc.volunteermgtsystem.repositories.ActivityRepository;
import com.lfc.volunteermgtsystem.repositories.MinistryRepository;
import com.lfc.volunteermgtsystem.repositories.UserRepository;
import com.lfc.volunteermgtsystem.repositories.VolunteerAssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportingService {
    
    private UserRepository userRepository;
    private ActivityRepository activityRepository;

    private VolunteerAssignmentRepository assignmentRepository;
    private MinistryRepository ministryRepository;
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalVolunteers", userRepository.countByRoleAndActive(Role.VOLUNTEER));
        stats.put("totalMinistries", ministryRepository.findAllActive().size());
        stats.put("upcomingActivities", activityRepository.findUpcomingActivities(LocalDateTime.now()).size());
        stats.put("activitiesNeedingVolunteers", activityRepository.findActivitiesNeedingVolunteers().size());
        
        return stats;
    }
    
    public Map<String, Object> getVolunteerReport(Long volunteerId, LocalDateTime startDate, LocalDateTime endDate) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new RuntimeException("Volunteer not found"));
        
        List<VolunteerAssignment> assignments = assignmentRepository.findByDateRange(startDate, endDate)
                .stream()
                .filter(assignment -> assignment.getVolunteer().getId().equals(volunteerId))
                .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("volunteer", getVolunteerSummary(volunteer));
        report.put("totalAssignments", assignments.size());
        report.put("completedAssignments", assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.COMPLETED ? 1 : 0)
                .sum());
        report.put("cancelledAssignments", assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.CANCELLED ? 1 : 0)
                .sum());
        report.put("noShowAssignments", assignments.stream()
                .mapToLong(a -> a.getStatus() == AssignmentStatus.NO_SHOW ? 1 : 0)
                .sum());
        report.put("totalHours", assignments.stream()
                .filter(a -> a.getHoursServed() != null)
                .mapToDouble(VolunteerAssignment::getHoursServed)
                .sum());
        report.put("assignments", assignments.stream()
                .map(this::getAssignmentSummary)
                .collect(Collectors.toList()));
        
        return report;
    }
    
    public Map<String, Object> getMinistryReport(Long ministryId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Activity> activities = activityRepository.findByDateRange(startDate, endDate)
                .stream()
                .filter(activity -> activity.getMinistry().getId().equals(ministryId))
                .collect(Collectors.toList());
        
        List<VolunteerAssignment> assignments = activities.stream()
                .flatMap(activity -> activity.getAssignments().stream())
                .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalActivities", activities.size());
        report.put("totalAssignments", assignments.size());
        report.put("uniqueVolunteers", assignments.stream()
                .map(a -> a.getVolunteer().getId())
                .collect(Collectors.toSet()).size());
        report.put("totalHours", assignments.stream()
                .filter(a -> a.getHoursServed() != null)
                .mapToDouble(VolunteerAssignment::getHoursServed)
                .sum());
        report.put("activitiesByStatus", getActivitiesByStatus(activities));
        report.put("assignmentsByStatus", getAssignmentsByStatus(assignments));
        report.put("activities", activities.stream()
                .map(this::getActivitySummary)
                .collect(Collectors.toList()));
        
        return report;
    }
    
    public Map<String, Object> getVolunteerParticipationReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<VolunteerAssignment> assignments = assignmentRepository.findByDateRange(startDate, endDate);
        
        Map<Long, List<VolunteerAssignment>> volunteerAssignments = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getVolunteer().getId()));
        
        List<Map<String, Object>> volunteerStats = volunteerAssignments.entrySet().stream()
                .map(entry -> {
                    User volunteer = userRepository.findById(entry.getKey()).orElse(null);
                    List<VolunteerAssignment> volAssignments = entry.getValue();
                    
                    Map<String, Object> stats = new HashMap<>();
                    stats.put("volunteer", getVolunteerSummary(volunteer));
                    stats.put("totalAssignments", volAssignments.size());
                    stats.put("completedAssignments", volAssignments.stream()
                            .mapToLong(a -> a.getStatus() == AssignmentStatus.COMPLETED ? 1 : 0)
                            .sum());
                    stats.put("totalHours", volAssignments.stream()
                            .filter(a -> a.getHoursServed() != null)
                            .mapToDouble(VolunteerAssignment::getHoursServed)
                            .sum());
                    
                    return stats;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("totalHours"), (Double) a.get("totalHours")))
                .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalVolunteers", volunteerStats.size());
        report.put("totalAssignments", assignments.size());
        report.put("totalHours", assignments.stream()
                .filter(a -> a.getHoursServed() != null)
                .mapToDouble(VolunteerAssignment::getHoursServed)
                .sum());
        report.put("volunteerStats", volunteerStats);
        
        return report;
    }
    
    public Map<String, Object> getVolunteerCoverageReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Activity> activities = activityRepository.findByDateRange(startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("totalActivities", activities.size());
        report.put("fullyStaffedActivities", activities.stream()
                .mapToLong(a -> a.getVolunteersAssigned() >= a.getVolunteersNeeded() ? 1 : 0)
                .sum());
        report.put("partiallyStaffedActivities", activities.stream()
                .mapToLong(a -> a.getVolunteersAssigned() > 0 && a.getVolunteersAssigned() < a.getVolunteersNeeded() ? 1 : 0)
                .sum());
        report.put("unstaffedActivities", activities.stream()
                .mapToLong(a -> a.getVolunteersAssigned() == 0 ? 1 : 0)
                .sum());
        
        List<Map<String, Object>> activityCoverage = activities.stream()
                .map(activity -> {
                    Map<String, Object> coverage = new HashMap<>();
                    coverage.put("activity", getActivitySummary(activity));
                    coverage.put("volunteersNeeded", activity.getVolunteersNeeded());
                    coverage.put("volunteersAssigned", activity.getVolunteersAssigned());
                    coverage.put("coveragePercentage", 
                            activity.getVolunteersNeeded() > 0 ? 
                                    (double) activity.getVolunteersAssigned() / activity.getVolunteersNeeded() * 100 : 0);
                    return coverage;
                })
                .sorted((a, b) -> Double.compare((Double) a.get("coveragePercentage"), (Double) b.get("coveragePercentage")))
                .collect(Collectors.toList());
        
        report.put("activityCoverage", activityCoverage);
        
        return report;
    }
    
    public List<Map<String, Object>> getMonthlyVolunteerHours(int year) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);
            
            List<VolunteerAssignment> assignments = assignmentRepository.findByDateRange(startDate, endDate);
            double totalHours = assignments.stream()
                    .filter(a -> a.getHoursServed() != null)
                    .mapToDouble(VolunteerAssignment::getHoursServed)
                    .sum();
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", month);
            monthData.put("monthName", startDate.format(DateTimeFormatter.ofPattern("MMMM")));
            monthData.put("totalHours", totalHours);
            monthData.put("totalAssignments", assignments.size());
            monthData.put("uniqueVolunteers", assignments.stream()
                    .map(a -> a.getVolunteer().getId())
                    .collect(Collectors.toSet()).size());
            
            monthlyData.add(monthData);
        }
        
        return monthlyData;
    }
    
    private Map<String, Object> getVolunteerSummary(User volunteer) {
        if (volunteer == null) return null;
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", volunteer.getId());
        summary.put("firstName", volunteer.getFirstName());
        summary.put("lastName", volunteer.getLastName());
        summary.put("email", volunteer.getEmail());
        return summary;
    }
    
    private Map<String, Object> getActivitySummary(Activity activity) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", activity.getId());
        summary.put("name", activity.getName());
        summary.put("startTime", activity.getStartTime());
        summary.put("endTime", activity.getEndTime());
        summary.put("location", activity.getLocation());
        summary.put("status", activity.getStatus());
        summary.put("ministry", activity.getMinistry().getName());
        return summary;
    }
    
    private Map<String, Object> getAssignmentSummary(VolunteerAssignment assignment) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", assignment.getId());
        summary.put("activity", getActivitySummary(assignment.getActivity()));
        summary.put("status", assignment.getStatus());
        summary.put("assignedAt", assignment.getAssignedAt());
        summary.put("hoursServed", assignment.getHoursServed());
        summary.put("isSubstitute", assignment.getIsSubstitute());
        return summary;
    }
    
    private Map<String, Long> getActivitiesByStatus(List<Activity> activities) {
        return activities.stream()
                .collect(Collectors.groupingBy(
                        activity -> activity.getStatus().toString(),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> getAssignmentsByStatus(List<VolunteerAssignment> assignments) {
        return assignments.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getStatus().toString(),
                        Collectors.counting()
                ));
    }
}