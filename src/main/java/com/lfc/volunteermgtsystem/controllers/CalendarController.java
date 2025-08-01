package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.serviceImpls.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CalendarController {

    private CalendarService calendarService;
    
    @GetMapping("/view")
    public ResponseEntity<Map<String, Object>> getCalendarView(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long ministryId,
            @RequestParam(required = false) Long volunteerId) {
        
        Map<String, Object> calendar = calendarService.getCalendarView(startDate, endDate, ministryId, volunteerId);
        return ResponseEntity.ok(calendar);
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyCalendar(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long ministryId,
            @RequestParam(required = false) Long volunteerId) {
        
        Map<String, Object> calendar = calendarService.getMonthlyCalendar(year, month, ministryId, volunteerId);
        return ResponseEntity.ok(calendar);
    }
    
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate,
            @RequestParam(required = false) Long ministryId,
            @RequestParam(required = false) Long volunteerId) {
        
        Map<String, Object> calendar = calendarService.getWeeklyCalendar(weekStartDate, ministryId, volunteerId);
        return ResponseEntity.ok(calendar);
    }
    
    @GetMapping("/daily")
    public ResponseEntity<Map<String, Object>> getDailyCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long ministryId,
            @RequestParam(required = false) Long volunteerId) {
        
        Map<String, Object> calendar = calendarService.getDailyCalendar(date, ministryId, volunteerId);
        return ResponseEntity.ok(calendar);
    }
    
    @GetMapping("/my-schedule")
    public ResponseEntity<List<Map<String, Object>>> getMySchedule(
            @AuthenticationPrincipal User currentUser,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Map<String, Object>> schedule = calendarService.getVolunteerSchedule(currentUser.getId(), startDate, endDate);
        return ResponseEntity.ok(schedule);
    }
    
    @GetMapping("/volunteer/{volunteerId}/schedule")
    public ResponseEntity<List<Map<String, Object>>> getVolunteerSchedule(
            @PathVariable Long volunteerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Map<String, Object>> schedule = calendarService.getVolunteerSchedule(volunteerId, startDate, endDate);
        return ResponseEntity.ok(schedule);
    }
    
    @GetMapping("/my-upcoming")
    public ResponseEntity<List<Map<String, Object>>> getMyUpcomingAssignments(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "30") int days) {
        
        List<Map<String, Object>> assignments = calendarService.getUpcomingAssignments(currentUser.getId(), days);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/volunteer/{volunteerId}/upcoming")
    public ResponseEntity<List<Map<String, Object>>> getVolunteerUpcomingAssignments(
            @PathVariable Long volunteerId,
            @RequestParam(defaultValue = "30") int days) {
        
        List<Map<String, Object>> assignments = calendarService.getUpcomingAssignments(volunteerId, days);
        return ResponseEntity.ok(assignments);
    }
    
    @GetMapping("/my-ical")
    public ResponseEntity<String> getMyICalFeed(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(3);
        }
        
        String icalContent = calendarService.generateICalFeed(currentUser.getId(), startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "volunteer-schedule.ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
    }
    
    @GetMapping("/volunteer/{volunteerId}/ical")
    public ResponseEntity<String> getVolunteerICalFeed(
            @PathVariable Long volunteerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusMonths(3);
        }
        
        String icalContent = calendarService.generateICalFeed(volunteerId, startDate, endDate);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "volunteer-schedule.ics");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(icalContent);
    }
}