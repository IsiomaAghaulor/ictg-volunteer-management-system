package com.lfc.volunteermgtsystem.controllers;

import com.lfc.volunteermgtsystem.entities.Ministry;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.UserMinistry;
import com.lfc.volunteermgtsystem.enums.MinistryRole;
import com.lfc.volunteermgtsystem.serviceImpls.MinistryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ministries")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MinistryController {
    
    @Autowired
    private MinistryService ministryService;
    
    @GetMapping
    public ResponseEntity<Page<Ministry>> getAllMinistries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Ministry> ministries = ministryService.getAllActiveMinistries(pageable);
        return ResponseEntity.ok(ministries);
    }
    
    @GetMapping("/list")
    public ResponseEntity<List<Ministry>> getAllMinistriesList() {
        List<Ministry> ministries = ministryService.getAllActiveMinistries();
        return ResponseEntity.ok(ministries);
    }
    
    @GetMapping("/root")
    public ResponseEntity<List<Ministry>> getRootMinistries() {
        List<Ministry> rootMinistries = ministryService.getRootMinistries();
        return ResponseEntity.ok(rootMinistries);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ministry> getMinistryById(@PathVariable Long id) {
        Optional<Ministry> ministry = ministryService.findById(id);
        return ministry.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/subministries")
    public ResponseEntity<List<Ministry>> getSubMinistries(@PathVariable Long id) {
        List<Ministry> subMinistries = ministryService.getSubMinistries(id);
        return ResponseEntity.ok(subMinistries);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Ministry>> searchMinistries(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Ministry> ministries = ministryService.searchMinistries(q, pageable);
        return ResponseEntity.ok(ministries);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> createMinistry(@Valid @RequestBody Ministry ministry) {
        try {
            Ministry createdMinistry = ministryService.createMinistry(ministry);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMinistry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create ministry");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> updateMinistry(@PathVariable Long id, @Valid @RequestBody Ministry ministry) {
        try {
            Ministry updatedMinistry = ministryService.updateMinistry(id, ministry);
            return ResponseEntity.ok(updatedMinistry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update ministry");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deactivateMinistry(@PathVariable Long id) {
        try {
            ministryService.deactivateMinistry(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ministry deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to deactivate ministry");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/{ministryId}/members/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> assignUserToMinistry(
            @PathVariable Long ministryId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "VOLUNTEER") MinistryRole role) {
        try {
            UserMinistry userMinistry = ministryService.assignUserToMinistry(userId, ministryId, role);
            return ResponseEntity.ok(userMinistry);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to assign user to ministry");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/{ministryId}/members/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('MINISTRY_LEADER')")
    public ResponseEntity<?> removeUserFromMinistry(@PathVariable Long ministryId, @PathVariable Long userId) {
        try {
            ministryService.removeUserFromMinistry(userId, ministryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User removed from ministry successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to remove user from ministry");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/{id}/members")
    public ResponseEntity<Page<User>> getMinistryMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> members = ministryService.getMinistryMembers(id, pageable);
        return ResponseEntity.ok(members);
    }
}