package com.lfc.volunteermgtsystem.serviceImpls;

import com.lfc.volunteermgtsystem.entities.Notification;
import com.lfc.volunteermgtsystem.entities.User;
import com.lfc.volunteermgtsystem.entities.VolunteerAssignment;
import com.lfc.volunteermgtsystem.enums.NotificationChannel;
import com.lfc.volunteermgtsystem.enums.NotificationType;
import com.lfc.volunteermgtsystem.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    

    private JavaMailSender emailSender;
    
    public Notification createNotification(User recipient, String title, String message,
                                           NotificationType type,
                                           NotificationChannel channel) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setChannel(channel);
        
        notification = notificationRepository.save(notification);
        
        if (channel == NotificationChannel.EMAIL) {
            sendEmailNotification(notification);
        }
        
        return notification;
    }
    
    @Async
    public void sendEmailNotification(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient().getEmail());
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            
            emailSender.send(message);
            
            notification.setSentStatus(true);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }
    
    public void sendAssignmentNotification(VolunteerAssignment assignment) {
        String title = "New Volunteer Assignment";
        String message = String.format(
                "You have been assigned to: %s\n" +
                "Date: %s\n" +
                "Time: %s - %s\n" +
                "Location: %s\n\n" +
                "Thank you for your service!",
                assignment.getActivity().getName(),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getLocation() != null ? assignment.getActivity().getLocation() : "TBD"
        );
        
        createNotification(assignment.getVolunteer(), title, message, 
                         NotificationType.ASSIGNMENT,
                         NotificationChannel.EMAIL);
    }
    
    public void sendSignUpConfirmationNotification(VolunteerAssignment assignment) {
        String title = "Volunteer Sign-up Confirmation";
        String message = String.format(
                "Thank you for signing up for: %s\n" +
                "Date: %s\n" +
                "Time: %s - %s\n" +
                "Location: %s\n\n" +
                "We appreciate your willingness to serve!",
                assignment.getActivity().getName(),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getLocation() != null ? assignment.getActivity().getLocation() : "TBD"
        );
        
        createNotification(assignment.getVolunteer(), title, message, 
                         NotificationType.CONFIRMATION,
                         NotificationChannel.EMAIL);
    }
    
    public void sendConfirmationNotification(VolunteerAssignment assignment) {
        String title = "Assignment Confirmed";
        String message = String.format(
                "Your assignment has been confirmed for: %s\n" +
                "Date: %s\n" +
                "Time: %s - %s\n\n" +
                "Looking forward to seeing you there!",
                assignment.getActivity().getName(),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getEndTime().format(DateTimeFormatter.ofPattern("h:mm a"))
        );
        
        createNotification(assignment.getVolunteer(), title, message, 
                         NotificationType.CONFIRMATION,
                         NotificationChannel.EMAIL);
    }
    
    public void sendCancellationNotification(VolunteerAssignment assignment) {
        String title = "Assignment Cancelled";
        String message = String.format(
                "Your assignment for: %s has been cancelled.\n" +
                "Date: %s\n" +
                "Time: %s - %s\n\n" +
                "Thank you for your understanding.",
                assignment.getActivity().getName(),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getEndTime().format(DateTimeFormatter.ofPattern("h:mm a"))
        );
        
        createNotification(assignment.getVolunteer(), title, message, 
                         NotificationType.CANCELLATION,
                         NotificationChannel.EMAIL);
    }
    
    public void sendReminderNotification(VolunteerAssignment assignment) {
        String title = "Upcoming Assignment Reminder";
        String message = String.format(
                "Reminder: You have an upcoming assignment for: %s\n" +
                "Date: %s\n" +
                "Time: %s - %s\n" +
                "Location: %s\n\n" +
                "See you there!",
                assignment.getActivity().getName(),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                assignment.getActivity().getStartTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getEndTime().format(DateTimeFormatter.ofPattern("h:mm a")),
                assignment.getActivity().getLocation() != null ? assignment.getActivity().getLocation() : "TBD"
        );
        
        createNotification(assignment.getVolunteer(), title, message, 
                         NotificationType.REMINDER,
                         NotificationChannel.EMAIL);
    }
    
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientId(userId, pageable);
    }
    
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByRecipientId(userId);
    }
    
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setReadStatus(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        for (Notification notification : unreadNotifications) {
            notification.setReadStatus(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByRecipientId(userId);
    }
    
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}