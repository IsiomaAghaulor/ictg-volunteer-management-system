package com.lfc.volunteermgtsystem.repositories;

import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.active = true")
    Page<User> findAllActive(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    Page<User> findByRoleAndActive(@Param("role") Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "u.active = true")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.userMinistries um WHERE um.ministry.id = :ministryId AND um.active = true")
    Page<User> findByMinistryId(@Param("ministryId") Long ministryId, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true AND u.role = :role")
    Long countByRoleAndActive(@Param("role") Role role);
}