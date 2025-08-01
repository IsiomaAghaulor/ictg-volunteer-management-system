package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.dto.UserRegistrationRequest;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.VolunteerProfile;
import com.lfc.volunteermgtsystem.enums.Role;
import com.lfc.volunteermgtsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return user;
    }
    
    public User createUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        
        user = userRepository.save(user);
        
        if (user.getRole() == Role.VOLUNTEER) {
            VolunteerProfile profile = new VolunteerProfile();
            profile.setUser(user);
            profile.setEmergencyContactName(request.getEmergencyContactName());
            profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
            profile.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
            profile.setSkills(request.getSkills());
            profile.setInterests(request.getInterests());
            profile.setBio(request.getBio());
            
            user.setVolunteerProfile(profile);
            user = userRepository.save(user);
        }
        
        return user;
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable);
    }
    
    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRoleAndActive(role, pageable);
    }
    
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchActiveUsers(searchTerm, pageable);
    }
    
    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setProfilePhotoUrl(updatedUser.getProfilePhotoUrl());
        
        return userRepository.save(existingUser);
    }
    
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }
    
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public Long getTotalVolunteerCount() {
        return userRepository.countByRoleAndActive(Role.VOLUNTEER);
    }
}