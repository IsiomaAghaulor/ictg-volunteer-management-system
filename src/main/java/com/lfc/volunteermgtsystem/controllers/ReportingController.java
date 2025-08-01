package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.serviceImpls.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReportingController {
    
    @Autowired
    private ReportingService reportingService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = reportingService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/volunteer/{volunteerId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> getVolunteerReport(
            @PathVariable Long volunteerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> report = reportingService.getVolunteerReport(volunteerId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/ministry/{ministryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<Map<String, Object>> getMinistryReport(
            @PathVariable Long ministryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> report = reportingService.getMinistryReport(ministryId, startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/volunteer-participation")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> getVolunteerParticipationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> report = reportingService.getVolunteerParticipationReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/volunteer-coverage")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<Map<String, Object>> getVolunteerCoverageReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> report = reportingService.getVolunteerCoverageReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/monthly-hours")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER') or hasRole('VOLUNTEER_COORDINATOR')")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyVolunteerHours(@RequestParam int year) {
        List<Map<String, Object>> monthlyData = reportingService.getMonthlyVolunteerHours(year);
        return ResponseEntity.ok(monthlyData);
    }
}