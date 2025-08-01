package com.lfc.volunteermgtsystem.repositories;

import com.lfc.volunteermgtsystem.entities.Activity;
import com.lfc.volunteermgtsystem.enums.ActivityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    
    @Query("SELECT a FROM Activity a WHERE a.ministry.id = :ministryId ORDER BY a.startTime ASC")
    Page<Activity> findByMinistryId(@Param("ministryId") Long ministryId, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.startTime >= :startTime AND a.endTime <= :endTime ORDER BY a.startTime ASC")
    List<Activity> findByDateRange(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status ORDER BY a.startTime ASC")
    Page<Activity> findByStatus(@Param("status") ActivityStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.volunteersAssigned < a.volunteersNeeded ORDER BY a.startTime ASC")
    List<Activity> findActivitiesNeedingVolunteers();
    
    @Query("SELECT a FROM Activity a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Activity> searchActivities(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT a FROM Activity a JOIN a.assignments va WHERE va.volunteer.id = :volunteerId ORDER BY a.startTime ASC")
    Page<Activity> findByVolunteerId(@Param("volunteerId") Long volunteerId, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.startTime >= :fromDate ORDER BY a.startTime ASC")
    List<Activity> findUpcomingActivities(@Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT COUNT(a) FROM Activity a WHERE a.ministry.id = :ministryId AND a.status = :status")
    Long countByMinistryIdAndStatus(@Param("ministryId") Long ministryId, 
                                   @Param("status") ActivityStatus status);
}