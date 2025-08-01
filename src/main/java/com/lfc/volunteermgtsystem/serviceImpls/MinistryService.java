package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.entities.Ministry;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.UserMinistry;
import com.lfc.volunteermgtsystem.enums.MinistryRole;
import com.lfc.volunteermgtsystem.repositories.MinistryRepository;
import com.lfc.volunteermgtsystem.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MinistryService {
    
    @Autowired
    private MinistryRepository ministryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public Ministry createMinistry(Ministry ministry) {
        if (ministryRepository.existsByNameAndActiveTrue(ministry.getName())) {
            throw new RuntimeException("Ministry with this name already exists");
        }
        return ministryRepository.save(ministry);
    }
    
    public Optional<Ministry> findById(Long id) {
        return ministryRepository.findById(id);
    }
    
    public List<Ministry> getAllActiveMinistries() {
        return ministryRepository.findAllActive();
    }
    
    public Page<Ministry> getAllActiveMinistries(Pageable pageable) {
        return ministryRepository.findAllActive(pageable);
    }
    
    public List<Ministry> getRootMinistries() {
        return ministryRepository.findRootMinistries();
    }
    
    public List<Ministry> getSubMinistries(Long parentId) {
        return ministryRepository.findByParentMinistryId(parentId);
    }
    
    public Page<Ministry> searchMinistries(String searchTerm, Pageable pageable) {
        return ministryRepository.searchActiveMinistries(searchTerm, pageable);
    }
    
    public Ministry updateMinistry(Long id, Ministry updatedMinistry) {
        Ministry existingMinistry = ministryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministry not found"));
        
        existingMinistry.setName(updatedMinistry.getName());
        existingMinistry.setDescription(updatedMinistry.getDescription());
        existingMinistry.setRequirements(updatedMinistry.getRequirements());
        existingMinistry.setParentMinistry(updatedMinistry.getParentMinistry());
        
        return ministryRepository.save(existingMinistry);
    }
    
    public void deactivateMinistry(Long id) {
        Ministry ministry = ministryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministry not found"));
        ministry.setActive(false);
        ministryRepository.save(ministry);
    }
    
    public void activateMinistry(Long id) {
        Ministry ministry = ministryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministry not found"));
        ministry.setActive(true);
        ministryRepository.save(ministry);
    }
    
    public UserMinistry assignUserToMinistry(Long userId, Long ministryId, MinistryRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Ministry ministry = ministryRepository.findById(ministryId)
                .orElseThrow(() -> new RuntimeException("Ministry not found"));
        
        UserMinistry userMinistry = new UserMinistry();
        userMinistry.setUser(user);
        userMinistry.setMinistry(ministry);
        userMinistry.setRole(role);
        
        user.getUserMinistries().add(userMinistry);
        ministry.getUserMinistries().add(userMinistry);
        
        userRepository.save(user);
        return userMinistry;
    }
    
    public void removeUserFromMinistry(Long userId, Long ministryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserMinistry userMinistry = user.getUserMinistries().stream()
                .filter(um -> um.getMinistry().getId().equals(ministryId) && um.getActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User is not assigned to this ministry"));
        
        userMinistry.setActive(false);
        userRepository.save(user);
    }
    
    public Page<User> getMinistryMembers(Long ministryId, Pageable pageable) {
        return userRepository.findByMinistryId(ministryId, pageable);
    }
}