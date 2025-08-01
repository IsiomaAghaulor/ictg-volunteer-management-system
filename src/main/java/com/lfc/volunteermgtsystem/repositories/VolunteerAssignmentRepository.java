package com.lfc.volunteermgtsystem.repositories;

import com.lfc.volunteermgtsystem.entities.VolunteerAssignment;
import com.lfc.volunteermgtsystem.enums.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VolunteerAssignmentRepository extends JpaRepository<VolunteerAssignment, Long> {
    
    @Query("SELECT va FROM VolunteerAssignment va WHERE va.volunteer.id = :volunteerId ORDER BY va.activity.startTime ASC")
    Page<VolunteerAssignment> findByVolunteerId(@Param("volunteerId") Long volunteerId, Pageable pageable);
    
    @Query("SELECT va FROM VolunteerAssignment va WHERE va.activity.id = :activityId")
    List<VolunteerAssignment> findByActivityId(@Param("activityId") Long activityId);
    
    @Query("SELECT va FROM VolunteerAssignment va WHERE va.status = :status")
    Page<VolunteerAssignment> findByStatus(@Param("status") AssignmentStatus status, Pageable pageable);
    
    @Query("SELECT va FROM VolunteerAssignment va WHERE " +
           "va.volunteer.id = :volunteerId AND " +
           "va.activity.startTime <= :endTime AND " +
           "va.activity.endTime >= :startTime AND " +
           "va.status <> 'CANCELLED'")
    List<VolunteerAssignment> findConflictingAssignments(@Param("volunteerId") Long volunteerId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT va FROM VolunteerAssignment va WHERE " +
           "va.activity.startTime >= :startDate AND va.activity.startTime < :endDate")
    List<VolunteerAssignment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(va.hoursServed) FROM VolunteerAssignment va WHERE " +
           "va.volunteer.id = :volunteerId AND va.status = 'COMPLETED'")
    Double getTotalHoursServedByVolunteer(@Param("volunteerId") Long volunteerId);
    
    @Query("SELECT COUNT(va) FROM VolunteerAssignment va WHERE " +
           "va.volunteer.id = :volunteerId AND va.status = 'COMPLETED'")
    Long getCompletedAssignmentCountByVolunteer(@Param("volunteerId") Long volunteerId);
}