package com.smartproject.platform.service;

import com.smartproject.platform.model.*;
import com.smartproject.platform.repository.NotificationRepository;
import com.smartproject.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public Notification createNotification(UUID userId, NotificationType type, String title, 
                                          String message, String entityType, UUID entityId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .entityType(entityType)
                .entityId(entityId)
                .isRead(false)
                .build();
        
        return notificationRepository.save(notification);
    }
    
    public void notifyApprovalRequest(User approver, Approval approval) {
        createNotification(
                approver.getId(),
                NotificationType.APPROVAL_REQUEST,
                "Approval Request",
                String.format("You have a pending approval request for %s", approval.getEntityType()),
                approval.getEntityType(),
                approval.getEntityId()
        );
    }
    
    public void notifyRoleChange(User user, Role newRole, Project project) {
        createNotification(
                user.getId(),
                NotificationType.ROLE_CHANGE,
                "Role Updated",
                String.format("Your role in project '%s' has been updated to %s", 
                        project.getName(), newRole),
                "PROJECT",
                project.getId()
        );
    }
    
    public void notifyTaskAssignment(User assignee, Task task) {
        createNotification(
                assignee.getId(),
                NotificationType.TASK_ASSIGNED,
                "Task Assigned",
                String.format("You have been assigned to task: %s", task.getTitle()),
                "TASK",
                task.getId()
        );
    }
    
    public void notifyDecisionApproval(User decisionMaker, DecisionLog decision) {
        createNotification(
                decisionMaker.getId(),
                NotificationType.DECISION_APPROVED,
                "Decision Approved",
                String.format("Your decision '%s' has been approved", decision.getTitle()),
                "DECISION",
                decision.getId()
        );
    }
    
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Page<Notification> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = getUnreadNotifications(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }
}
