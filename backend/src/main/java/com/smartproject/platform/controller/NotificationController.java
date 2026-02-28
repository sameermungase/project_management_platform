package com.smartproject.platform.controller;

import com.smartproject.platform.model.Notification;
import com.smartproject.platform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Notifications", description = "User notification endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }
    
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get user notifications (paginated)")
    public ResponseEntity<Page<Notification>> getNotificationsPaginated(
            @RequestParam UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId, pageable));
    }
    
    @GetMapping("/unread")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }
    
    @GetMapping("/unread/count")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Long> getUnreadCount(@RequestParam UUID userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }
    
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            @RequestParam UUID userId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/read-all")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('MANAGER') or hasAuthority('ADMIN')")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@RequestParam UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
