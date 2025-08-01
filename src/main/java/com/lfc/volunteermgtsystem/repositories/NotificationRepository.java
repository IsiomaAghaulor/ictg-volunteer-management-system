package com.lfc.volunteermgtsystem.repositories;

import com.lfc.volunteermgtsystem.entities.Notification;
import com.lfc.volunteermgtsystem.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.readStatus = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("recipientId") Long recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.sentStatus = false")
    List<Notification> findUnsentNotifications();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.readStatus = false")
    Long countUnreadByRecipientId(@Param("recipientId") Long recipientId);
    
    @Query("SELECT n FROM Notification n WHERE n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByType(@Param("type") NotificationType type, Pageable pageable);
}