package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.entities.Activity;
import com.lfc.volunteermgtsystem.entities.VolunteerAssignment;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import com.lfc.volunteermgtsystem.repositories.ActivityRepository;
import com.lfc.volunteermgtsystem.repositories.VolunteerAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CalendarService {
    
    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private VolunteerAssignmentRepository assignmentRepository;
    
    public Map<String, Object> getCalendarView(LocalDate startDate, LocalDate endDate, Long ministryId, Long volunteerId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Activity> activities = activityRepository.findByDateRange(startDateTime, endDateTime);
        
        if (ministryId != null) {
            activities = activities.stream()
                    .filter(activity -> activity.getMinistry().getId().equals(ministryId))
                    .collect(Collectors.toList());
        }
        
        List<Map<String, Object>> calendarEvents = new ArrayList<>();
        
        for (Activity activity : activities) {
            Map<String, Object> event = createCalendarEvent(activity);
            
            if (volunteerId != null) {
                boolean isAssigned = activity.getAssignments().stream()
                        .anyMatch(assignment -> assignment.getVolunteer().getId().equals(volunteerId) &&
                                assignment.getStatus() != AssignmentStatus.CANCELLED);
                event.put("isAssigned", isAssigned);
                
                if (isAssigned) {
                    VolunteerAssignment assignment = activity.getAssignments().stream()
                            .filter(a -> a.getVolunteer().getId().equals(volunteerId) &&
                                    a.getStatus() != AssignmentStatus.CANCELLED)
                            .findFirst().orElse(null);
                    if (assignment != null) {
                        event.put("assignmentStatus", assignment.getStatus());
                        event.put("assignmentId", assignment.getId());
                    }
                }
            }
            
            calendarEvents.add(event);
        }
        
        Map<String, Object> calendarData = new HashMap<>();
        calendarData.put("events", calendarEvents);
        calendarData.put("startDate", startDate);
        calendarData.put("endDate", endDate);
        
        return calendarData;
    }
    
    public Map<String, Object> getMonthlyCalendar(int year, int month, Long ministryId, Long volunteerId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        Map<String, Object> calendar = getCalendarView(startDate, endDate, ministryId, volunteerId);
        
        Map<String, List<Map<String, Object>>> eventsByDate = new HashMap<>();
        List<Map<String, Object>> events = (List<Map<String, Object>>) calendar.get("events");
        
        for (Map<String, Object> event : events) {
            LocalDateTime startTime = (LocalDateTime) event.get("startTime");
            String dateKey = startTime.toLocalDate().toString();
            
            eventsByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(event);
        }
        
        calendar.put("eventsByDate", eventsByDate);
        calendar.put("year", year);
        calendar.put("month", month);
        calendar.put("monthName", yearMonth.getMonth().name());
        calendar.put("daysInMonth", yearMonth.lengthOfMonth());
        
        return calendar;
    }
    
    public Map<String, Object> getWeeklyCalendar(LocalDate weekStartDate, Long ministryId, Long volunteerId) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        Map<String, Object> calendar = getCalendarView(weekStartDate, weekEndDate, ministryId, volunteerId);
        
        List<LocalDate> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(weekStartDate.plusDays(i));
        }
        
        Map<String, List<Map<String, Object>>> eventsByDay = new HashMap<>();
        List<Map<String, Object>> events = (List<Map<String, Object>>) calendar.get("events");
        
        for (Map<String, Object> event : events) {
            LocalDateTime startTime = (LocalDateTime) event.get("startTime");
            String dayKey = startTime.getDayOfWeek().name();
            
            eventsByDay.computeIfAbsent(dayKey, k -> new ArrayList<>()).add(event);
        }
        
        calendar.put("eventsByDay", eventsByDay);
        calendar.put("weekDays", weekDays);
        calendar.put("weekStartDate", weekStartDate);
        calendar.put("weekEndDate", weekEndDate);
        
        return calendar;
    }
    
    public Map<String, Object> getDailyCalendar(LocalDate date, Long ministryId, Long volunteerId) {
        Map<String, Object> calendar = getCalendarView(date, date, ministryId, volunteerId);
        
        List<Map<String, Object>> events = (List<Map<String, Object>>) calendar.get("events");
        events.sort((a, b) -> ((LocalDateTime) a.get("startTime")).compareTo((LocalDateTime) b.get("startTime")));
        
        calendar.put("date", date);
        calendar.put("dayOfWeek", date.getDayOfWeek().name());
        
        return calendar;
    }
    
    public List<Map<String, Object>> getVolunteerSchedule(Long volunteerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<VolunteerAssignment> assignments = assignmentRepository.findByDateRange(startDateTime, endDateTime)
                .stream()
                .filter(assignment -> assignment.getVolunteer().getId().equals(volunteerId) &&
                                    assignment.getStatus() != AssignmentStatus.CANCELLED)
                .collect(Collectors.toList());
        
        return assignments.stream()
                .map(this::createScheduleEvent)
                .sorted(Comparator.comparing(a -> ((LocalDateTime) a.get("startTime"))))
                .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getUpcomingAssignments(Long volunteerId, int days) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(days);
        
        List<VolunteerAssignment> assignments = assignmentRepository.findByDateRange(startDate, endDate)
                .stream()
                .filter(assignment -> assignment.getVolunteer().getId().equals(volunteerId) &&
                                    assignment.getStatus() != AssignmentStatus.CANCELLED)
                .collect(Collectors.toList());
        
        return assignments.stream()
                .map(this::createScheduleEvent)
                .sorted(Comparator.comparing(a -> ((LocalDateTime) a.get("startTime"))))
                .collect(Collectors.toList());
    }
    
    public String generateICalFeed(Long volunteerId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> schedule = getVolunteerSchedule(volunteerId, startDate, endDate);
        
        StringBuilder ical = new StringBuilder();
        ical.append("BEGIN:VCALENDAR\n");
        ical.append("VERSION:2.0\n");
        ical.append("PRODID:-//Church CMS//Volunteer Schedule//EN\n");
        ical.append("CALSCALE:GREGORIAN\n");
        
        for (Map<String, Object> event : schedule) {
            ical.append("BEGIN:VEVENT\n");
            ical.append("UID:").append(event.get("assignmentId")).append("@church-cms.com\n");
            ical.append("DTSTART:").append(formatDateTimeForICal((LocalDateTime) event.get("startTime"))).append("\n");
            ical.append("DTEND:").append(formatDateTimeForICal((LocalDateTime) event.get("endTime"))).append("\n");
            ical.append("SUMMARY:").append(event.get("activityName")).append("\n");
            ical.append("DESCRIPTION:").append(event.get("description") != null ? event.get("description") : "").append("\n");
            ical.append("LOCATION:").append(event.get("location") != null ? event.get("location") : "").append("\n");
            ical.append("STATUS:").append(event.get("status")).append("\n");
            ical.append("END:VEVENT\n");
        }
        
        ical.append("END:VCALENDAR\n");
        
        return ical.toString();
    }
    
    private Map<String, Object> createCalendarEvent(Activity activity) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", activity.getId());
        event.put("title", activity.getName());
        event.put("description", activity.getDescription());
        event.put("startTime", activity.getStartTime());
        event.put("endTime", activity.getEndTime());
        event.put("location", activity.getLocation());
        event.put("status", activity.getStatus());
        event.put("ministry", activity.getMinistry().getName());
        event.put("ministryId", activity.getMinistry().getId());
        event.put("volunteersNeeded", activity.getVolunteersNeeded());
        event.put("volunteersAssigned", activity.getVolunteersAssigned());
        event.put("needsVolunteers", activity.getVolunteersAssigned() < activity.getVolunteersNeeded());
        
        return event;
    }
    
    private Map<String, Object> createScheduleEvent(VolunteerAssignment assignment) {
        Map<String, Object> event = new HashMap<>();
        event.put("assignmentId", assignment.getId());
        event.put("activityId", assignment.getActivity().getId());
        event.put("activityName", assignment.getActivity().getName());
        event.put("description", assignment.getActivity().getDescription());
        event.put("startTime", assignment.getActivity().getStartTime());
        event.put("endTime", assignment.getActivity().getEndTime());
        event.put("location", assignment.getActivity().getLocation());
        event.put("status", assignment.getStatus());
        event.put("ministry", assignment.getActivity().getMinistry().getName());
        event.put("ministryId", assignment.getActivity().getMinistry().getId());
        event.put("isSubstitute", assignment.getIsSubstitute());
        event.put("notes", assignment.getNotes());
        
        if (assignment.getActivityRole() != null) {
            event.put("role", assignment.getActivityRole().getName());
        }
        
        return event;
    }
    
    private String formatDateTimeForICal(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
    }
}